package com.easylaw.app.data.models

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class DiagnosisDetailResponse(
    @SerializedName("법령") val lawInfo: DiagnosisInfo? = null,
)

data class DiagnosisInfo(
    @SerializedName("기본정보") val basicInfo: BasicInfo? = null,
    @SerializedName("조문") val articles: ArticleWrapper? = null,
)

data class BasicInfo(
    @SerializedName("법령명_한글") val lawName: String? = null,
    @SerializedName("시행일자") val enforceDate: String? = null,
)

data class ArticleWrapper(
    @SerializedName("조문단위") val articleList: List<ArticleItem> = emptyList(),
)

data class ArticleItem(
    @SerializedName("조문번호") val articleNo: String? = null,
    @SerializedName("조문내용") val articleContent: String? = null,
    @SerializedName("항") val paragraphs: JsonElement? = null,
)
