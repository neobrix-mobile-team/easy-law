package com.easylaw.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CommunityLikeModel(
    // 기본키 (자동 생성되므로 생성 시에는 null 가능)
    val id: Long? = null,
    // 대상 게시글 ID
    val community_id: Long,
    // 좋아요를 누른 유저 UUID (서버에서 auth.uid()로 채워짐)
    val user_id: String? = null,
    // 생성 일시
    val created_at: String? = null,
)
