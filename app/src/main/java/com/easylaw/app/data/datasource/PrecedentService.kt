package com.easylaw.app.data.datasource

import android.util.Log
import com.easylaw.app.data.repository.PrecedentAiRepository
import com.easylaw.app.util.PreferenceManager
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject

class PrecedentService
    @Inject
    constructor(
        private val generativeModel: GenerativeModel,
        private val preferenceManager: PreferenceManager,
    ) : PrecedentAiRepository {
        private fun resolveErrorCause(e: Exception): String =
            when {
                e is kotlinx.coroutines.TimeoutCancellationException -> "응답 시간 초과"
                e.message?.contains("429") == true || e.message?.contains("quota") == true -> "사용량 한도 초과"
                e.message?.contains("401") == true || e.message?.contains("403") == true -> "API 인증 실패"
                e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("timeout") == true -> "네트워크 오류"

                else -> "알 수 없는 오류"
            }

        // ✅ extractKeyword는 한국 법령 API 검색용이므로 언어와 무관하게 항상 한국어 키워드만 추출
        // ✅ summarizePrecedent만 다국어 지원 (사용자에게 노출되는 결과물이므로)
        private suspend fun summaryLanguageInstruction(): String =
            when (preferenceManager.getLanguage()) {
                "ko" ->
                    """

                    [출력 언어와 구조]
                    전체 응답은 한국어로만 작성해야 합니다.
                    다음 섹션 헤더를 정확히 사용합니다:
                    사례 요약 / [사례 배경] / [주요 이슈] / [판결] / [실무적 시사점]
                    """.trimIndent()

                "en" ->
                    """

                    [OUTPUT LANGUAGE & STRUCTURE]
                    Your ENTIRE response MUST be written in English only. DO NOT use any Korean characters.
                    Use the following section headers exactly:
                    Case Summary / [Case Background] / [Key Issue] / [Ruling] / [Practical Implications]
                    """.trimIndent()

                "ja" ->
                    """

                    [出力言語と構造]
                    回答全体を必ず日本語のみで記述してください。韓国語を一切使用しないでください。
                    以下のセクション見出しをそのまま使用してください:
                    判決の要点 / [事件の背景] / [主要な争点] / [判決結果] / [実務上の意味]
                    """.trimIndent()

                else -> ""
            }

        override suspend fun extractKeyword(
            situation: String,
            details: String,
        ): String {
            val prompt =
                """
                당신은 한국의 '국가법령정보센터' 판례 검색 API에 최적화된 법률 키워드 추출기입니다.
                사용자의 상황을 분석하여, 검색 결과가 가장 많이 그리고 정확하게 나올 수 있는 핵심 법률 명사 1~2개를 추출하세요.
                
                [검색 API 특성 및 원칙]
                1. 분해 원칙: 복합어(예: 임금체불, 교통사고처리)보다는 분리된 핵심 단일 명사(예: 임금, 해고, 대여금)를 사용할 때 검색 성공률이 훨씬 높습니다. 
                2. 법률 표준어: 일상어는 배제하고, 실제 대법원 판결문에서 가장 빈번하게 사용되는 '표준 법률 단어'로 치환하세요.
                3. 띄어쓰기: 키워드를 2개 추출할 경우, 반드시 띄어쓰기 한 번으로만 구분하세요. (예: 임금 퇴직금)
                4. 절대 금지: 어떠한 경우에도 인사말, 부연 설명, 기호(따옴표, 마침표 등)를 출력하지 마세요. 오직 추출된 키워드 텍스트만 출력해야 합니다.
                5. 반드시 한국어로만 키워드를 출력하세요. (API가 한국어 키워드만 지원합니다)
                
                [변환 예시]
                - "임금이 석달째 밀렸어요" -> 임금
                - "월급을 못 받았어요" -> 임금
                - "집주인이 전세금을 안 돌려줘요" -> 임대차보증금
                - "돈 빌려줬는데 안 갚아요" -> 대여금
                - "술 먹고 운전하다 사고 냈어요" -> 음주운전
                - "회사가 갑자기 나가라고 해요" -> 해고
                - "단순 사고" -> 사고
                
                상황: $situation
                상세내용: $details
                """.trimIndent()
            return try {
                Log.d("PrecedentService_LOG", "[키워드 추출] Gemini 요청 시작")
                val response = generativeModel.generateContent(prompt)
                val keyword = response.text?.replace(Regex("[^가-힣a-zA-Z0-9 ]"), "")?.trim() ?: situation
                Log.d("PrecedentService_LOG", "[키워드 추출] 성공: [$keyword]")
                keyword
            } catch (e: Exception) {
                val cause = resolveErrorCause(e)
                Log.e("PrecedentService_LOG", "[키워드 추출] 실패 ($cause): ${e.message}")
                situation // 실패 시 원문 그대로 검색어로 사용
            }
        }

        override suspend fun summarizePrecedent(originalText: String): String {
            val prompt =
                """
                [역할 설정]
                당신은 복잡한 대법원 판결문을 법률 지식이 없는 일반인을 위해 '모바일 최적화' 형태로 요약하는 전문 법률 커뮤니케이터입니다.

                [요약 원칙 및 구조]
                - 대상 독자: 법률 용어에 익숙하지 않은 일반인.
                - 모바일 최적화: 한 문장은 20~25자 내외로 짧게 끊으세요.
                - 시각적 구성: 줄바꿈을 자주 사용하고 [ ]와 • 기호로 구역을 나누세요.
                - 언어 선택: 어려운 한자어나 전문 용어는 쉬운 일상어로 풀어서 쓰세요.
                
                [절대 금지]
                - 한자어가 섞인 딱딱한 문어체 사용 금지.
                - 30자 이상의 긴 문장 나열 금지.
                - "안녕하세요", "도움이 되길 바랍니다" 등 대화형 문구 금지
                - 전문 용어 나열 및 30자 이상의 장문 사용 금지
                - 마침표(.) 외의 불필요한 특수문자 남발 금지
                - ** 기호 사용 금지
                
                [출력 구조 가이드]
                
                판결 핵심 요약
                [(굵게) 판결의 결론을 한 문장으로 명확히 제시]
                
                [사건 배경]
                • 누가, 누구에게, 무엇을 했는지 핵심 요약
                • 발생한 사고와 적용된 혐의 요약
                
                [핵심 쟁점]
                • 법원에서 다투는 구체적인 질문 (과연 ~인가?)
                
                [판결 결과]
                • 결론: (인정/부정 등 최종 판단)
                • 이유:
                    - 대법원이 판단한 핵심 근거 (짧게)
                    - 법리적 해석의 핵심 내용
                
                [실무적 의미]
                • 이 판결로 인해 바뀌는 결과나 주의사항
                • 처벌 방식이나 기준의 변화
                
                [분석할 판례 원문]
                $originalText
                ${summaryLanguageInstruction()}
                """.trimIndent()

            // summarizePrecedent는 실패 시 throw → UseCase/ViewModel에서 사용자에게 에러 표시
            Log.d("PrecedentService_LOG", "[판례 요약] Gemini 요청 시작 - 원문 길이: ${originalText.length}자")
            val response = generativeModel.generateContent(prompt)
            val result = response.text?.trim() ?: throw IllegalStateException("AI 응답이 비어있습니다.")
            Log.d("PrecedentService_LOG", "[판례 요약] 성공 - 요약 길이: ${result.length}자")
            return result
        }
    }
