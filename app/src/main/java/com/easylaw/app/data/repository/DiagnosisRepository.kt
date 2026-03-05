package com.easylaw.app.data.repository

data class FollowUpAction(
    val isEnough: Boolean,
    val question: String = "",
    val options: List<String> = emptyList(),
)

interface DiagnosisRepository {
    suspend fun getAdditionalQuestions(scenario: String): FollowUpAction

    suspend fun extractTargetLaws(context: String): List<String>

    suspend fun fetchDiagnosisDetails(lawNames: List<String>): String

    suspend fun generateFinalGuide(
        scenario: String,
        lawDetails: String,
    ): String
}
