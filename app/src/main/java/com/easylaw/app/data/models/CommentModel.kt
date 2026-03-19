package com.easylaw.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CommentModel(
    val author: String = "",
    val content: String,
    val created_at: String = "",
)
