package com.easylaw.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityCommentModel(
    val id: Long? = null,
    val post_id: Long,
    val user_id: String,
    val author: String,
    val content: String,
    val parent_id: Long? = null,
    val created_at: String? = null,
    @SerialName("like_count")
    val likeCountList: List<LikeCountResponse> = emptyList(),
    @SerialName("is_liked")
    val likeUserList: List<LikeUserResponse> = emptyList(),
    val likeCount: Int = 0,
    val isLiked: Boolean = false,
    val rePlyCount: Int = 0,
    val replies: List<CommunityCommentModel> = emptyList(),
)

@Serializable
data class LikeCountResponse(
    val count: Int,
)

@Serializable
data class LikeUserResponse(
    val user_id: String,
)
