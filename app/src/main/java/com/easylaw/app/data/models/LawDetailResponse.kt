package com.easylaw.app.data.models

import com.google.gson.annotations.SerializedName

data class LawDetailResponse(
    @SerializedName("PrecService") val precService: PrecDetailItem?,
)

data class PrecDetailItem(
    @SerializedName("판례일련번호") val caseId: String?,
    @SerializedName("사건명") val title: String?,
    @SerializedName("판시사항") val issue: String?,
    @SerializedName("판결요지") val summary: String?,
    @SerializedName("판례내용") val content: String?,
)
