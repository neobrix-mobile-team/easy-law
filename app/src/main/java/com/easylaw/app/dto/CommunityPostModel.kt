package com.easylaw.app.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int? = null,
    val contentauthor: String,
    val tag: String,
    val title: String,
    val content: String,
    // db칼럼명과 다를 시 해줘야 매핑된다.
    @SerialName("created_at") val createdAt: String? = null,

    val comments: List<Comment> = emptyList()
) {
    val commentCount: String
        get() = comments.size.toString()
}

@Serializable
data class Comment(
    val id: Int? = null,
    @SerialName("post_id")
    val postId: Int,
    val author: String,
    val content: String,
    @SerialName("created_at") val createdAt: String? = null

)
