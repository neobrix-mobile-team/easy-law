package com.easylaw.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class DiagnosisDetailResponse(
    @SerialName("법령") val lawInfo: DiagnosisInfo? = null,
)

@Serializable
data class DiagnosisInfo(
    @SerialName("기본정보") val basicInfo: BasicInfo? = null,
    @SerialName("조문") val articles: ArticleWrapper? = null,
)

@Serializable
data class BasicInfo(
    @SerialName("법령명_한글") val lawName: String? = null,
    @SerialName("시행일자") val enforceDate: String? = null,
)

@Serializable
data class ArticleWrapper(
    @SerialName("조문단위") val articleList: List<ArticleItem> = emptyList(),
)

@Serializable
data class ArticleItem(
    @SerialName("조문번호") val articleNo: String? = null,
    @SerialName("조문내용") val articleContent: String? = null,
    @SerialName("항") val paragraphs: JsonElement? = null,
)
