package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.LawApiService
import com.google.ai.client.generativeai.GenerativeModel
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
    ) : DiagnosisRepository {
        override suspend fun getAdditionalQuestions(scenario: String): FollowUpAction =
            withContext(Dispatchers.IO) {
                val prompt =
                    """
                    당신은 전문 법률 상담가입니다. 사용자의 문제 상황을 분석하세요.
                    법률적 판단(예: 체당금 신청 가능 여부, 형사처벌 대상 여부, 계약 위반 여부 등)을 내리기 위해 **필수적인 추가 정보가 더 필요하다면** 아래 JSON 형식으로 질문을 1개만 생성하세요.
                    {"status": "NEED_INFO", "question": "상시 근로자 수가 5인 이상인가요?", "options": ["5인 이상", "5인 미만", "모름"]}
                    
                    엄격한 금지 규칙]
                    1. 지금까지의 대화 내역을 반드시 분석하여, 사용자가 이미 대답한 정보나 이전에 시스템이 했던 질문은 절대로 다시 묻지 마세요.
                    2. 똑같은 질문을 반복할 바에는 차라리 질문을 멈추고 "ENOUGH" 상태로 응답하세요.
                    
                    단, 핵심 정보가 충분히 수집되었거나, 의미 있는 질문이 없다면 아래 JSON으로만 응답하세요:
                    {"status": "ENOUGH"}
                    
                    지금까지의 대화 내역:
                    $scenario
                    
                    반드시 마크다운이나 다른 텍스트 없이 순수 JSON 형식만 출력하세요.
                    """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val responseText =
                    response.text
                        ?.replace("```json", "")
                        ?.replace("```", "")
                        ?.trim() ?: ""

                return@withContext try {
                    val jsonObject = JSONObject(responseText)
                    val status = jsonObject.optString("status")

                    if (status == "ENOUGH") {
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
                        FollowUpAction(isEnough = false, question = question, options = options)
                    }
                } catch (e: Exception) {
                    FollowUpAction(isEnough = true)
                }
            }

        override suspend fun extractTargetLaws(context: String): List<String> =
            withContext(Dispatchers.IO) {
                val prompt =
                    """
                    다음 대화 내용을 바탕으로 검색해야 할 '법령명'만 쉼표로 구분해서 추출해줘.
                    답변이나 설명은 절대 하지 마. (예시: 근로기준법, 임금채권보장법)
                    대화 내용: $context
                    """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                return@withContext response.text
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() } ?: emptyList()
            }

        override suspend fun fetchDiagnosisDetails(lawNames: List<String>): String =
            withContext(Dispatchers.IO) {
                val detailsBuilder = StringBuilder()
                val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월"))
                detailsBuilder.append("기준 시점: $currentDate (현행법령 기준)\n\n")

                val deferredLaws =
                    lawNames.map { lawName ->
                        async {
                            val localBuilder = StringBuilder()
                            try {
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
                                } else {
                                    localBuilder.append("[$lawName] 일치하는 현행 법령을 찾을 수 없습니다.\n\n")
                                }
                            } catch (e: Exception) {
                                localBuilder.append("[$lawName] 현행 법령 정보를 가져오는 데 실패했습니다.\n")
                            }
                        }
                    }

                val lawResults = deferredLaws.awaitAll()
                lawResults.forEach { detailsBuilder.append(it) }

                detailsBuilder.append("\n주의: 위 내용은 현행법 기준입니다.")
                val rawText = detailsBuilder.toString()
                return@withContext rawText.replace(Regex("\\s+"), " ").trim()
            }

        override suspend fun generateFinalGuide(
            scenario: String,
            lawDetails: String,
        ): String =
            withContext(Dispatchers.IO) {
                val prompt =
                    """
                    너는 취약계층을 돕는 친절한 법률 전문가야. 
                    사용자의 상황과 제공된 [관련 법령 및 조항]을 바탕으로 아래 [필수 규칙]을 엄격하게 지켜 답변을 작성해.
                    
                    [필수 규칙]
                    1. 결론 먼저: 현재 사용자가 처한 상황을 한 줄로 진단할 것. (예: "현재 상황은 임금체불에 해당합니다.")
                    2. 3단계 행동 지침: 사용자가 해결을 위해 당장 해야 할 일을 우선순위대로 딱 3가지만 기호를 달아 제시할 것. 
                        - 각 지침은 1~2문장으로 아주 짧게
                        - 필수적으로 해야할 일이 3가지 이상일 경우 추가적으로 제시
                        - 강조 표기(매우 중요): 사용자가 반드시 기억해야 할 핵심 단어, 제출해야 할 서류명, 경고 사항 등은 반드시 앞뒤로 `**` 기호를 붙여 강조할 것. (예: "가장 먼저 **고용노동부 진정서**를 제출해야 합니다.") 
                    3. 친절한 언행: 어렵고 힘든 사용자를 향한 따뜻하게 위로하고 격려하는 말투 사용.
                    4. 금기사항: 어려운 법률 용어는 무조건 쉬운 말로 풀어서 쓰고, 전체 글이 스마트폰 한 화면에 들어오도록 최대한 간결하게 할 것. 장황한 법리 해석 절대 금지.
                    
                    [예시]=============================
                    
                    현재 상황은 임금체불에 해당합니다.
                    
                    [이렇게 해보세요]
                    ● 증거 확보 : 밀린 급여 내역, 실제로 일했다는 증거를 최대한 모으기
                    ● 지급 요구 : 사장님께 밀린 급여를 달라고 내용증명 우편이나 문자등으로 공식적으로 요청(요청내용, 보낸증거 수집)
                    ● 노동청 진정 : 위 증거들을 가지고 가까운 노동청에 '임금체불 진정'신청 
                    
                    현재 상태에서 회사를 구만두면 채불임금을 받는 과정이 더 복잡해질 수 있으니 신중하게 결정하세요.  
                    
                    ===================================
                    
                    
                    사용자 상황: $scenario
                    관련 법령: $lawDetails
                    """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                Log.d("gemini_response", "$response")
                return@withContext response.text ?: "가이드를 생성하는 데 문제가 발생했습니다."
            }
    }
