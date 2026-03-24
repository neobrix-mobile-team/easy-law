package com.easylaw.app.data.repository

interface PrecedentAiRepository {
    /**
     * 사용자 입력에서 판례 검색 최적 키워드를 추출합니다.
     * 실패 시 원문 situation을 그대로 반환합니다.
     */
    suspend fun extractKeyword(
        situation: String,
        details: String,
    ): List<String>

    /**
     * 판례 원문을 일반인이 이해하기 쉬운 형태로 요약합니다.
     * @throws Exception AI 호출 실패 시 — 호출부에서 처리
     */
    suspend fun summarizePrecedent(
        originalText: String,
        language: String,
        onChunk: (String) -> Unit,
    ): String
}
