package com.easylaw.app.data.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityWriteModel(
    // 기본키
    val id: Long? = null,
    // 로그인 유저의 id
    val user_id: String? = null,
    val created_at: String = "",
    val category: String,
    val title: String,
    val content: String,
    val author: String,
//    val images: List<String> = emptyList(),
    @EncodeDefault
    val images: List<String> = emptyList(),
    /*
    val comments: List<CommentModel> = emptyList(),

    검색 쿼리에서 수까지 같이 받아온다.
     */
    @SerialName("comment_count")
    val commentCountList: List<LikeCountResponse> = emptyList(),
    @SerialName("comment_like")
    val commentLikeList: List<LikeCountResponse> = emptyList(),
    @SerialName("root_comment_count")
    val rootCommentCount: Int? = null,
) {
    val commentCount: Int
        get() = rootCommentCount ?: (commentCountList.firstOrNull()?.count ?: 0)
    val likeCount: Int
        get() = commentLikeList.firstOrNull()?.count ?: 0
}
