package com.easylaw.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoryModel(
    val id: Long? = null,
    @SerialName("category_key") val key: String, // 내부 로직용 (예: "CIVIL")
    @SerialName("category_name") val name: String, // UI 표시용 (예: "민사")
)
