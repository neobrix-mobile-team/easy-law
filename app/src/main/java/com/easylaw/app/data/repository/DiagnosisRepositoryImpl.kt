package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.LawApiService
import com.easylaw.app.domain.model.FollowUpAction
import com.easylaw.app.util.PreferenceManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DiagnosisRepositoryImpl
    @Inject
    constructor(
        private val apiService: LawApiService,
        private val generativeModel: GenerativeModel,
        private val preferenceManager: PreferenceManager,
    ) : DiagnosisRepository {
        private fun jsonLanguagePrefix(language: String): String =
            when (language) {
                "en" -> "[SYSTEM] You MUST respond ONLY in English. Do NOT use Korean under any circumstances.\n"
                "ja" -> "[システム] 必ず日本語のみで回答してください。韓国語は一切使用しないでください。\n"
                else -> ""
            }

        private fun jsonLanguageSuffix(language: String): String =
            when (language) {
                "en" ->
                    """
                    [CRITICAL REMINDER]
                    - Your ENTIRE response MUST be written in English only.
                    - All "question" and "options" values in JSON must be in English.
                    - JSON keys must remain as-is.
                    - DO NOT use any Korean characters.
                    """.trimIndent()

                "ja" ->
                    """
                    [最終確認]
                    - 回答全体を必ず日本語のみで記述してください。
                    - JSONの "question" と "options" の値も日本語で記述してください。
                    - JSONのキーはそのままにしてください。
                    - 韓国語を一切使用しないでください。
                    """.trimIndent()

                else -> ""
            }

        private fun guideLanguagePrefix(language: String): String =
            when (language) {
                "en" -> "[SYSTEM] You MUST respond ONLY in English. Do NOT use Korean under any circumstances.\n"
                "ja" -> "[システム] 必ず日本語のみで回答してください。韓国語は一切使用しないでください。\n"
                else -> ""
            }

        private fun guideLanguageSuffix(language: String): String =
            when (language) {
                "en" ->
                    """
                    [CRITICAL REMINDER]
                    - Your ENTIRE response MUST be written in English only.
                    - DO NOT use any Korean characters.
                    """.trimIndent()

                "ja" ->
                    """
                    [最終確認]
                    - 回答全体を必ず日本語のみで記述してください。
                    - 韓国語を一切使用しないでください。
                    """.trimIndent()

                else -> ""
            }

        private fun List<Content>.toContextText(): String =
            joinToString("\n") { content ->
                content.parts
                    .filterIsInstance<TextPart>()
                    .joinToString("") { it.text }
            }

        override suspend fun getAdditionalQuestions(
            history: List<Content>,
            language: String,
        ): FollowUpAction =
            withContext(Dispatchers.IO) {
                val systemPrompt =
                    """
                    ${jsonLanguagePrefix(language)}
                    당신은 전문 법률 상담가입니다. 사용자의 문제 상황을 분석하세요.
                    법률적 판단(예: 체당금 신청 가능 여부, 형사처벌 대상 여부, 계약 위반 여부 등)을 내리기 위해 **필수적인 추가 정보가 더 필요하다면** 아래 JSON 형식으로 질문을 1개만 생성하세요.
                    {"status": "NEED_INFO", "question": "상시 근로자 수가 5인 이상인가요?", "options": ["5인 이상", "5인 미만", "모름"]}
                    
                    [엄격한 금지 규칙]
                    1. 지금까지의 대화 내역을 반드시 분석하여, 사용자가 이미 대답한 정보나 이전에 시스템이 했던 질문은 절대로 다시 묻지 마세요.
                    2. 똑같은 질문을 반복할 바에는 차라리 질문을 멈추고 "ENOUGH" 상태로 응답하세요.
                    
                    단, 핵심 정보가 충분히 수집되었거나, 의미 있는 질문이 없다면 아래 JSON으로만 응답하세요:
                    {"status": "ENOUGH"}
                    
                    반드시 마크다운이나 다른 텍스트 없이 순수 JSON 형식만 출력하세요.
                    ${jsonLanguageSuffix(language)}
                    """.trimIndent()

                val fullHistory = history + content("user") { text(systemPrompt) }

                Log.d("Diagnosis_LOG", "[1단계] getAdditionalQuestions 시작, language: $language, history 길이: ${history.size}")
                return@withContext try {
                    val response = generativeModel.generateContent(*fullHistory.toTypedArray())
                    val responseText =
                        response.text
                            ?.replace("```json", "")
                            ?.replace("```", "")
                            ?.trim() ?: ""
                    Log.d("Diagnosis_LOG", "[1단계] Gemini 응답: $responseText")

                    try {
                        val jsonObject = JSONObject(responseText)
                        val status = jsonObject.optString("status")

                        if (status == "ENOUGH") {
                            Log.d("Diagnosis_LOG", "[1단계] 상태: ENOUGH → 최종 분석으로 진행")
                            FollowUpAction(isEnough = true)
                        } else {
                            val question = jsonObject.optString("question", "추가 정보가 필요합니다.")
                            val optionsArray = jsonObject.optJSONArray("options")
                            val options = mutableListOf<String>()
                            if (optionsArray != null) {
                                for (i in 0 until optionsArray.length()) {
                                    options.add(optionsArray.getString(i))
                                }
                            }
                            Log.d("Diagnosis_LOG", "[1단계] 추가 질문 생성: $question")
                            FollowUpAction(isEnough = false, question = question, options = options)
                        }
                    } catch (e: Exception) {
                        Log.e("Diagnosis_LOG", "[1단계] JSON 파싱 실패 → ENOUGH 처리: ${e.message}")
                        FollowUpAction(isEnough = true)
                    }
                } catch (e: Exception) {
                    Log.e("Diagnosis_LOG", "[1단계] Gemini API 호출 실패: ${e.javaClass.simpleName} - ${e.message}")
                    throw e
                }
            }

        override suspend fun extractTargetLaws(history: List<Content>): List<String> =
            withContext(Dispatchers.IO) {
                // 법령 추출은 항상 한국어 키워드가 필요하므로 언어 지시 없이 텍스트만 추출
                val contextText = history.toContextText()
                val prompt =
                    """
                    다음 대화 내용을 바탕으로 검색해야 할 '법령명'만 쉼표로 구분해서 추출해줘.
                    답변이나 설명은 절대 하지 마. (예시: 근로기준법, 임금채권보장법)
                    대화 내용: $contextText
                    """.trimIndent()

                Log.d("Diagnosis_LOG", "[2단계] prompt: $prompt")
                Log.d("Diagnosis_LOG", "[2단계] extractTargetLaws 시작")
                return@withContext try {
                    val response = generativeModel.generateContent(prompt)
                    val laws =
                        response.text
                            ?.split(",")
                            ?.map { it.trim() }
                            ?.filter { it.isNotEmpty() } ?: emptyList()
                    Log.d("Diagnosis_LOG", "[2단계] 추출된 법령: $laws")
                    laws
                } catch (e: Exception) {
                    Log.e("Diagnosis_LOG", "[2단계] 법령 추출 실패: ${e.javaClass.simpleName} - ${e.message}")
                    throw e
                }
            }

        override suspend fun fetchDiagnosisDetails(lawNames: List<String>): String =
            withContext(Dispatchers.IO) {
                Log.d("Diagnosis_LOG", "[3단계] fetchDiagnosisDetails 시작 - 법령 수: ${lawNames.size}")
                val detailsBuilder = StringBuilder()
                val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월"))
                detailsBuilder.append("기준 시점: $currentDate (현행법령 기준)\n\n")

                val deferredLaws =
                    lawNames.map { lawName ->
                        async {
                            val localBuilder = StringBuilder()
                            try {
                                Log.d("Diagnosis_LOG", "[3단계] 법령 API 호출: $lawName")
                                val listResponse = apiService.getStatuteList(keyword = lawName)
                                val mst =
                                    listResponse.lawSearch
                                        ?.law
                                        ?.firstOrNull { it.currentHistoryCode == "현행" }
                                        ?.lawSeq
                                val lawId =
                                    listResponse.lawSearch
                                        ?.law
                                        ?.firstOrNull { it.currentHistoryCode == "현행" }
                                        ?.lawId

                                if (mst != null) {
                                    val detailResponse = apiService.getStatuteDetail(mst = mst, lawId = lawId)
                                    val actualLawName = detailResponse.lawInfo?.basicInfo?.lawName ?: lawName
                                    localBuilder.append("[$actualLawName 현행본문]\n")

                                    detailResponse.lawInfo?.articles?.articleList?.take(3)?.forEach { article ->
                                        localBuilder.append("${article.articleContent} ")
                                        val paragraphs = article.paragraphs
                                        if (paragraphs is JsonArray) {
                                            paragraphs.forEach { paragraph ->
                                                val content = paragraph.jsonObject["항내용"]?.jsonPrimitive?.content
                                                if (content != null) localBuilder.append("  $content ")
                                            }
                                        } else if (paragraphs is JsonObject) {
                                            val content = paragraphs["항내용"]?.jsonPrimitive?.content
                                            if (content != null) localBuilder.append("  $content ")
                                        }
                                    }
                                    localBuilder.append("\n\n")
                                    Log.d("Diagnosis_LOG", "[3단계] 법령 조회 성공: $actualLawName")
                                } else {
                                    localBuilder.append("[$lawName] 일치하는 현행 법령을 찾을 수 없습니다.\n\n")
                                    Log.w("Diagnosis_LOG", "[3단계] 현행 법령 없음: $lawName")
                                }
                            } catch (e: Exception) {
                                localBuilder.append("[$lawName] 현행 법령 정보를 가져오는 데 실패했습니다.\n")
                                Log.e("Diagnosis_LOG", "[3단계] 직렬화 실패 상세: ${e.javaClass.name} - ${e.message}")
                                Log.e("Diagnosis_LOG", "[3단계] 법령 API 실패 [$lawName]: ${e.javaClass.simpleName} - ${e.message}")
                            }
                            localBuilder.toString()
                        }
                    }

                val lawResults = deferredLaws.awaitAll()
                lawResults.forEach { detailsBuilder.append(it) }

                detailsBuilder.append("\n주의: 위 내용은 현행법 기준입니다.")
                val rawText = detailsBuilder.toString()
                Log.d("Diagnosis_LOG", "[3단계] 법령 수집 완료 - 총 길이: ${rawText.length}자")
                return@withContext rawText.replace(Regex("\\s+"), " ").trim()
            }

        override suspend fun generateFinalGuide(
            history: List<Content>,
            lawDetails: String,
            language: String,
            onChunk: (String) -> Unit,
        ): String =
            withContext(Dispatchers.IO) {
                val contextText = history.toContextText()

                val prompt =
                    """
                    ${guideLanguagePrefix(language)}
                    너는 취약계층을 돕는 친절한 법률 전문가야. 
                    사용자의 상황과 [관련 법령]을 바탕으로 아래 [필수 규칙]을 엄격하게 지켜 답변을 작성해.
                    
                    [필수 규칙]
                    1. 결론 먼저: 현재 상황을 한 줄로 진단할 것. (예: "현재 상황은 임금체불에 해당합니다.")
                    2. 3단계 행동 지침: 해결을 위해 당장 해야 할 일을 우선순위대로 3가지만 기호를 달아 제시.
                        - 각 지침은 1~2문장으로 짧게
                        - 필수 사항이 3가지 이상이면 추가 제시 가능
                        - 핵심 단어, 서류명, 경고 사항은 반드시 `**`로 강조 (예: **고용노동부 진정서**)
                    3. 친절한 말투: 어렵고 힘든 사용자를 따뜻하게 위로하고 격려할 것.
                    4. 금기사항: 어려운 법률 용어는 쉬운 말로 풀어 쓰고, 스마트폰 한 화면에 들어오도록 간결하게. 장황한 법리 해석 금지.
                    
                    사용자 상황: $contextText
                    관련 법령: $lawDetails
                    ${guideLanguageSuffix(language)}
                    """.trimIndent()

                Log.d("Diagnosis_LOG", "[4단계] generateFinalGuide 스트리밍 시작 - language: $language")
                return@withContext try {
                    val sb = StringBuilder()
                    generativeModel.generateContentStream(prompt).collect { chunk ->
                        val text = chunk.text ?: return@collect
                        sb.append(text)
                        onChunk(text)
                    }
                    val result = sb.toString()
                    Log.d("Diagnosis_LOG", "[4단계] 스트리밍 완료 - 길이: ${result.length}자")
                    result
                } catch (e: Exception) {
                    Log.e("Diagnosis_LOG", "[4단계] 최종 가이드 생성 실패: ${e.javaClass.simpleName} - ${e.message}")
                    throw e
                }
            }
    }
