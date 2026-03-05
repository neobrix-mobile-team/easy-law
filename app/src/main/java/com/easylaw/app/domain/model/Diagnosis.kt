package com.easylaw.app.domain.model

sealed class Diagnosis {
    data class User(
        val text: String,
    ) : Diagnosis()

    data class Bot(
        val text: String,
    ) : Diagnosis()

    data class BotWithOptions(
        val text: String,
        val options: List<String>,
    ) : Diagnosis()

    data class ErrorRetry(
        val text: String,
        val retryActionType: RetryActionType,
    ) : Diagnosis()

    object Loading : Diagnosis()
}

enum class DiagnosisPhase {
    IDLE, // 기본 상태
    AWAITING_ANSWERS, // LLM의 추가 질문에 대한 사용자의 답변 대기 중
    PROCESSING, // 데이터 추출 및 API 통신 중
}

enum class RetryActionType {
    FOLLOW_UP_QUESTIONS,
    FINAL_GUIDE,
}
