package com.easylaw.app.data.repository

import com.easylaw.app.domain.model.FollowUpAction
import com.google.ai.client.generativeai.type.Content

interface DiagnosisRepository {
    // 사용자 문제 분석 후 추가질문 생성
    suspend fun getAdditionalQuestions(
        history: List<Content>,
        language: String,
    ): FollowUpAction

    // 문제에서 법령 조회용 키워드 추출
    suspend fun extractTargetLaws(history: List<Content>): List<String>

    // 법령 본문 조회
    suspend fun fetchDiagnosisDetails(lawNames: List<String>): String

    // 해결 지침 출력
    suspend fun generateFinalGuide(
        history: List<Content>,
        lawDetails: String,
        language: String,
        onChunk: (String) -> Unit,
    ): String
}
