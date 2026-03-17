package com.easylaw.app.domain.model

data class FollowUpAction(
    val isEnough: Boolean,
    val question: String = "",
    val options: List<String> = emptyList(),
)
