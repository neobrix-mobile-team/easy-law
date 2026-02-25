package com.easylaw.app.dto

import com.google.gson.annotations.SerializedName

// 판례 검색 응답 모델
data class LawApiResModel(
    @SerializedName("PrecSearch")
    val precSearch: PrecSearchModel,
)

data class PrecSearchModel(
    @SerializedName("키워드")
    val keyword: String,
    @SerializedName("page")
    val page: String,
    @SerializedName("target")
    val target: String,
    @SerializedName("totalCnt")
    val totalCnt: String,
    @SerializedName("section")
    val section: String,
    @SerializedName("prec")
    val prec: List<precModel>,
)

data class precModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("사건번호")
    val caseNumber: String,
    @SerializedName("사건명")
    val caseName: String,
    @SerializedName("선고일자")
    val judgmentDate: String,
    @SerializedName("법원명")
    val courtName: String,
    @SerializedName("판례일련번호")
    val serialNumber: String,
    @SerializedName("사건종류명")
    val caseTypeName: String,
    @SerializedName("판례상세링크")
    val detailLink: String,
    @SerializedName("데이터출처명")
    val dataSource: String,
    @SerializedName("판결유형")
    val judgment: String,
)
