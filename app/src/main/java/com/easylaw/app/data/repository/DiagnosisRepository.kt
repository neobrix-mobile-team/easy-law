package com.easylaw.app.data.repository

import com.easylaw.app.domain.model.FollowUpAction

interface DiagnosisRepository {
    // 사용자 문제 분석 후 추가질문 생성
    suspend fun getAdditionalQuestions(scenario: String): FollowUpAction

    // 문제에서 법령 조회용 키워드 추출
    suspend fun extractTargetLaws(context: String): List<String>

    // 법령 본문 조회
    suspend fun fetchDiagnosisDetails(lawNames: List<String>): String

    // 해결 지침 출력
    suspend fun generateFinalGuide(
        scenario: String,
        lawDetails: String,
    ): String
}
