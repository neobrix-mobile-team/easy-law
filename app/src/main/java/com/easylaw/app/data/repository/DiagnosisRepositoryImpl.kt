package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.LawApiService
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
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
                    
                    단, 지금까지의 대화 내역을 보고 이미 핵심 정보가 충분히 수집되었거나, 더 이상 의미 있는 질문이 없다면 반드시 아래 JSON으로만 응답하세요:
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

                for (lawName in lawNames) {
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
                            detailsBuilder.append("[$actualLawName 현행본문]\n")

                            detailResponse.lawInfo?.articles?.articleList?.take(5)?.forEach { article ->
                                detailsBuilder.append("${article.articleContent}\n")
                                val paragraphs = article.paragraphs
                                if (paragraphs is JsonArray) {
                                    paragraphs.forEach { paragraph ->
                                        val content = paragraph.jsonObject["항내용"]?.jsonPrimitive?.content
                                        if (content != null) detailsBuilder.append("  $content\n")
                                    }
                                } else if (paragraphs is JsonObject) {
                                    val content = paragraphs["항내용"]?.jsonPrimitive?.content
                                    if (content != null) detailsBuilder.append("  $content\n")
                                }
                            }
                            detailsBuilder.append("\n\n")
                        } else {
                            detailsBuilder.append("[$lawName] 일치하는 현행 법령을 찾을 수 없습니다.\n\n")
                        }
                    } catch (e: Exception) {
                        detailsBuilder.append("[$lawName] 현행 법령 정보를 가져오는 데 실패했습니다.\n")
                    }
                }
                detailsBuilder.append("\n주의: 위 내용은 현행법 기준입니다.")
                return@withContext detailsBuilder.toString()
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
                    2. 3단계 행동 지침: 사용자가 해결을 위해 당장 해야 할 일을 우선순위대로 딱 3가지만 번호표(1., 2., 3.)를 달아 제시할 것. (각 지침은 1~2문장으로 아주 짧게)
                    3. 친절한 한마디: 어렵고 힘든 사용자를 향한 따뜻한 격려 한 줄과 핵심 주의사항 하나 전달.
                    4. 금기사항: 어려운 법률 용어는 무조건 쉬운 말로 풀어서 쓰고, 전체 글이 스마트폰 한 화면에 들어오도록 최대한 간결하게 할 것. 장황한 법리 해석 절대 금지.
                    
                    사용자 상황: $scenario
                    관련 법령: $lawDetails
                    """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                Log.d("gemini_response", "$response")
                return@withContext response.text ?: "가이드를 생성하는 데 문제가 발생했습니다."
            }
    }
