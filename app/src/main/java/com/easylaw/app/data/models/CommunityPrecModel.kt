package com.easylaw.app.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityPrecModel(
    val id: String,
    @SerializedName("사건번호")
    val caseNumber: String,
    @SerializedName("데이터출처명")
    val source: String,
    @SerializedName("사건종류코드")
    val caseTypeCode: String,
    @SerializedName("사건종류명")
    val caseTypeName: String,
    @SerializedName("선고")
    val sentence: String,
    @SerializedName("선고일자")
    val sentenceDate: String,
    @SerializedName("판례일련번호")
    val caseSerialNumber: String,
    @SerializedName("판결유형")
    val judgmentType: String,
    @SerializedName("법원종류코드")
    val courtTypeCode: String,
    @SerializedName("법원명")
    val courtName: String,
    @SerializedName("판례상세링크")
    val detailLink: String,
    @SerializedName("사건명")
    val caseName: String,
)
