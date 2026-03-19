package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.util.PreferenceManager
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.TimeoutCancellationException
import javax.inject.Inject

class PrecedentRepositoryImpl
    @Inject
    constructor(
        private val generativeModel: GenerativeModel,
        private val preferenceManager: PreferenceManager,
    ) : PrecedentAiRepository {
        companion object {
            private const val MAX_KEYWORD_LENGTH = 10
            private const val MIN_KEYWORD_LENGTH = 1
        }

        private fun resolveErrorCause(e: Exception): String =
            when {
                e is TimeoutCancellationException -> "응답 시간 초과 e:$e"
                e.message?.contains("429") == true || e.message?.contains("quota") == true -> "사용량 한도 초과 e:$e"
                e.message?.contains("401") == true || e.message?.contains("403") == true -> "API 인증 실패 e:$e"
                e.message?.contains("Unable to resolve host") == true || e.message?.contains("timeout") == true -> "네트워크 오류 e:$e"

                else -> "알 수 없는 오류 e:$e"
            }

        private fun buildSummaryPrompt(
            originalText: String,
            language: String,
        ): String =
            when (language) {
                "en" ->
                    """
                    [SYSTEM] You MUST respond ONLY in English. Do NOT use Korean under any circumstances.

                    [Role]
                    You are a legal communicator who summarizes complex Supreme Court rulings in a mobile-optimized format for people without legal knowledge.

                    [Summarization Principles]
                    - Keep each sentence to around 20-25 characters
                    - Use frequent line breaks and organize sections with [ ] and • symbols
                    - Replace difficult legal terms with plain everyday language

                    [Strictly Prohibited]
                    - Stiff formal writing / sentences over 30 characters / conversational greetings / ** symbols

                    [Output Structure]
                    Case Summary
                    [Case Background] • Who did what to whom
                    [Key Issue] • The question the court is deciding
                    [Ruling] • Conclusion / Reasoning
                    [Practical Implications] • What changes or what to watch out for

                    [Original Ruling Text]
                    $originalText

                    [CRITICAL REMINDER]
                    Your ENTIRE response MUST be written in English only. DO NOT use any Korean characters.
                    Use the section headers listed above exactly as shown.
                    """.trimIndent()

                "ja" ->
                    """
                    [システム] 必ず日本語のみで回答してください。韓国語は一切使用しないでください。

                    [役割]
                    複雑な最高裁判決を法律知識のない一般人向けにモバイル最適化形式で要約する法律コミュニケーター。

                    [要約原則]
                    - 1文は20〜25字程度で短く区切ること
                    - 改行を多用し、[ ]と•記号でセクションを区切ること
                    - 難しい法律用語はわかりやすい日常語に言い換えること

                    [絶対禁止]
                    - 堅い文語体 / 30字以上の長文 / 会話調の挨拶 / **記号

                    [出力構造]
                    判決の要点
                    [事件の背景] • 誰が誰に何をしたか
                    [主要な争点] • 裁判所が判断する問い
                    [判決結果] • 結論／理由
                    [実務上の意味] • 変わること・注意すべきこと

                    [判例原文]
                    $originalText

                    [最終確認]
                    回答全体を必ず日本語のみで記述してください。韓国語を一切使用しないでください。
                    上記のセクション見出しをそのまま使用してください。
                    """.trimIndent()

                else -> // 한국어
                    """
                    [역할]
                    복잡한 대법원 판결문을 법률 지식이 없는 일반인을 위해 모바일 최적화 형태로 요약하는 법률 커뮤니케이터.

                    [요약 원칙]
                    - 한 문장은 20~25자 내외로 짧게 끊을 것
                    - 줄바꿈을 자주 사용하고 [ ]와 • 기호로 구역 구분
                    - 어려운 법률 용어는 쉬운 일상어로 풀어 쓸 것

                    [절대 금지]
                    - 딱딱한 문어체 / 30자 이상 장문 / 대화형 인사말 / ** 기호

                    [출력 구조]
                    판결 핵심 요약
                    [사건 배경] • 누가 누구에게 무엇을 했는지
                    [핵심 쟁점] • 법원에서 다투는 질문
                    [판결 결과] • 결론 / 이유
                    [실무적 의미] • 이 판결로 바뀌는 결과나 주의사항

                    [판례 원문]
                    $originalText

                    [출력 언어]
                    전체 응답은 한국어로만 작성해야 합니다.
                    """.trimIndent()
            }

        override suspend fun extractKeyword(
            situation: String,
            details: String,
        ): List<String> {
            val prompt =
                """
                당신은 한국의 '국가법령정보센터' 판례 검색 API에 최적화된 법률 키워드 추출기입니다.
                사용자의 상황을 분석하여, 검색 결과가 가장 많이 그리고 정확하게 나올 수 있는 핵심 법률 명사를 1~2개 추출하세요.
                
                [검색 API 특성 및 원칙]
                1. 분해 원칙: 복합어(예: 임금체불, 교통사고처리)보다는 분리된 핵심 단일 명사(예: 임금, 해고, 대여금)를 사용할 때 검색 성공률이 훨씬 높습니다.
                2. 법률 표준어: 일상어는 배제하고, 실제 대법원 판결문에서 가장 빈번하게 사용되는 '표준 법률 단어'로 치환하세요.
                3. 출력 형식: 반드시 아래 형식으로만 출력하세요. 그 외 어떠한 텍스트도 절대 출력하지 마세요.
                   키워드가 1개일 때: 임금
                   키워드가 2개일 때: 교통사고,횡단보도
                4. 절대 금지: 인사말, 부연 설명, 문장, 따옴표, 대괄호, JSON, 마침표 등 일체 금지
                5. 반드시 한국어로만 키워드를 출력하세요.
                
                [변환 예시]
                - "임금이 석달째 밀렸어요" -> 임금
                - "집주인이 전세금을 안 돌려줘요" -> 임대차보증금
                - "돈 빌려줬는데 안 갚아요" -> 대여금
                - "횡단보도를 건너다 차에 부딪혔어" -> 교통사고,횡단보도
                - "회사가 갑자기 나가라고 해요" -> 해고
                
                상황: $situation
                상세내용: $details
                """.trimIndent()
            return try {
                Log.d("PrecedentRepositoryImpl_LOG", "[키워드 추출] Gemini 요청 시작")
                val response = generativeModel.generateContent(prompt)
                val raw = response.text?.trim() ?: ""
                Log.d("PrecedentRepositoryImpl_LOG", "[키워드 추출] 원본 응답: [$raw]")

                val keywords = parseKeywords(raw, situation)
                Log.d("PrecedentRepositoryImpl_LOG", "[키워드 추출] 파싱 결과: $keywords")
                keywords
            } catch (e: Exception) {
                val cause = resolveErrorCause(e)
                Log.e("PrecedentRepositoryImpl_LOG", "[키워드 추출] 실패 ($cause): ${e.message}")
                listOf(situation.trim())
            }
        }

        private fun parseKeywords(
            raw: String,
            fallback: String,
        ): List<String> {
            // 한글/영문/숫자만 남기고 콤마는 구분자로 유지
            val cleaned =
                raw
                    .replace(Regex("```[\\s\\S]*?```"), "") // 코드블록 제거
                    .replace(Regex("[\"\\[\\]\\n]"), "") // JSON 기호 제거
                    .trim()

            // 콤마로 분리 시도
            val byComma = cleaned.split(",").map { it.replace(Regex("[^가-힣a-zA-Z0-9]"), "").trim() }.filter { it.isNotBlank() }

            if (byComma.isNotEmpty() && byComma.all { it.length in MIN_KEYWORD_LENGTH..MAX_KEYWORD_LENGTH }) {
                return byComma
            }

            // 공백으로 분리 시도 (단어가 짧을 때만 유효 키워드로 판단)
            val bySpace = cleaned.split(" ").map { it.replace(Regex("[^가-힣a-zA-Z0-9]"), "").trim() }.filter { it.isNotBlank() && it.length <= MAX_KEYWORD_LENGTH }

            if (bySpace.isNotEmpty()) {
                return bySpace.take(2) // 최대 2개만
            }

            // 모두 실패 시 원문 fallback
            Log.w("PrecedentRepositoryImpl_LOG", "[키워드 추출] 파싱 실패, fallback 사용: $fallback")
            return listOf(fallback.trim())
        }

        override suspend fun summarizePrecedent(
            originalText: String,
            language: String,
            onChunk: (String) -> Unit,
        ): String {
            val prompt = buildSummaryPrompt(originalText, language)

            Log.d("PrecedentRepositoryImpl_LOG", "[판례 요약] 스트리밍 시작 - 언어: $language, 원문 길이: ${originalText.length}자")
            val sb = StringBuilder()
            generativeModel.generateContentStream(prompt).collect { chunk ->
                val text = chunk.text ?: return@collect
                sb.append(text)
                onChunk(text)
            }
            val result = sb.toString().trim()
            if (result.isEmpty()) throw IllegalStateException("AI 응답이 비어있습니다.")
            Log.d("PrecedentRepositoryImpl_LOG", "[판례 요약] 현재 언어: ${preferenceManager.languageState.value}")
            Log.d("PrecedentRepositoryImpl_LOG", "[판례 요약] 결과: $result")
            Log.d("PrecedentRepositoryImpl_LOG", "[판례 요약] 완료 - 요약 길이: ${result.length}자")
            return result
        }
    }
