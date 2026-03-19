package com.easylaw.app.data.models

import com.google.gson.annotations.SerializedName

data class DiagnosisListResponse(
    @SerializedName("LawSearch") val lawSearch: DiagnosisSearch? = null,
)

data class DiagnosisSearch(
    @SerializedName("law") val law: List<DiagnosisItem> = emptyList(),
)

data class DiagnosisItem(
    @SerializedName("법령일련번호") val lawSeq: String? = null,
    @SerializedName("법령명한글") val lawName: String? = null,
    @SerializedName("법령ID") val lawId: String? = null,
    @SerializedName("시행일자") val enforceDate: String? = null,
    @SerializedName("현행연혁코드") val currentHistoryCode: String? = null,
)
