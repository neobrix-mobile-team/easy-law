package com.easylaw.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopCommenter(
    @SerialName("user_id")
    val id: String,
    @SerialName("user_name")
    val name: String,
    @SerialName("comment_count")
    val commentCount: Int,
)
