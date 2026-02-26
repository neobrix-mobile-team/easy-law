package com.easylaw.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PrecedentDetail(
    val caseId: String,
    val title: String,
    val issue: String,
    val summary: String,
    val content: String,
) {
    val fullTextForAi: String
        get() = "【판시사항】\n$issue\n\n【판결요지】\n$summary\n\n【판례내용】\n$content"
}
