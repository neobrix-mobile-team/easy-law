package com.easylaw.app.data.models

import com.google.gson.annotations.SerializedName

/**
 * 판례 검색 API 응답 모델
 *
 * 법원 판례 검색 API의 응답 데이터 구조를 정의합니다.
 */
data class LawApiResModel(
    @SerializedName("PrecSearch")
    val precSearch: PrecSearchModel,
)

/**
 * 판례 검색 결과 모델
 *
 * 검색 조건 및 판례 목록을 포함합니다.
 */
data class PrecSearchModel(
    @SerializedName("keyword")
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
    val prec: List<PrecModel>,
)

/**
 * 개별 판례 모델
 *
 * 판례의 기본 정보를 포함합니다.
 */
data class PrecModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("case_number")
    val caseNumber: String,
    @SerializedName("case_name")
    val caseName: String,
    @SerializedName("judgment_date")
    val judgmentDate: String,
    @SerializedName("court_name")
    val courtName: String,
    @SerializedName("serial_number")
    val serialNumber: String,
    @SerializedName("case_type_name")
    val caseTypeName: String,
    @SerializedName("detail_link")
    val detailLink: String,
    @SerializedName("data_source_name")
    val dataSourceName: String,
    @SerializedName("judgment_type")
    val judgmentType: String,
)
