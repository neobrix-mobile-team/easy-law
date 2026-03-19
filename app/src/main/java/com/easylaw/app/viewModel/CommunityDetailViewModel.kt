package com.easylaw.app.viewModel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.CommunityCommentModel
import com.easylaw.app.data.models.CommunityLikeModel
import com.easylaw.app.data.models.CommunityWriteModel
import com.easylaw.app.data.models.LikeUserResponse
import com.easylaw.app.domain.model.TopCommenter
import com.easylaw.app.domain.model.UserSession
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityDetailViewState(
    val userId: String = "",
    val communityDetail: CommunityWriteModel? = null,
    val isLoading: Boolean = false,
    val previewImage: String? = "",
    val showCommentSheet: Boolean = false,
    val commentInput: String = "",
    val isLiked: Boolean = false,
    val likeCount: Int = 0,
    val isReadOnly: Boolean = true,
    val communityToalComments: List<CommunityCommentModel> = emptyList(),
    val communityComments: List<CommunityCommentModel> = emptyList(),
    val isReplyMode: Boolean = false,
    val isSelectedReplyed: Long? = null,
    val parentComment: CommunityCommentModel? = null,
    val replyList: List<CommunityCommentModel> = emptyList(),
    val replyCount: Int = 0,
    val replyInput: String = "",
    val isOpenMoreSelected: Long? = null,
    val isEditMode: Boolean = false,
    val editCommentId: Long? = null,
    val isDeleteMode: Boolean = false,
    val deleteCommentId: Long? = null,
    val deleteInputText: String = "",
    val isCommunityDeleteMode: Boolean = false,
    val deleteCommunityInputText: String = "",
    val topCommenters: List<TopCommenter> = emptyList(),
//    val isCommunityDeleted: Boolean = false
)

@HiltViewModel
class CommunityDetailViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
    ) : ViewModel() {
    /*
        savedStateHandle: SavedStateHandle,
        자동으로 arguments를 가로챈다
     */
        private val id: Long = savedStateHandle.get<String>("id")?.toLongOrNull() ?: 0L

        private val _communityDetailViewState = MutableStateFlow(CommunityDetailViewState())
        val communityDetailViewState = _communityDetailViewState.asStateFlow()

        private val _isDeleteSuccess = Channel<Unit>()
        val isDeleteSuccess = _isDeleteSuccess.receiveAsFlow()

        init {
            // 부모의 생명주기 울타리
            viewModelScope.launch {
            /*
                자식 생명주기
                collect는 데이터를 계속 기다리는 작업
                별도의 작업공간(launch)을 만들어 작업을 병렬로 처리한다.
             */
                launch {
                    userSession.userInfo.collect { user ->
                        _communityDetailViewState.update {
                            it.copy(
                                isReadOnly = user.id.isEmpty(),
                                userId = user.id,
                            )
                        }
                    }
                }

//            loadCommunityDetail(id = id)
//            loadComments()
            }
        }

        fun refreshCommunityDetail() {
//        Log.d("read", _communityDetailViewState.value.isReadOnly.toString())
            viewModelScope.launch {
                loadCommunityDetail(id = id)
                loadTopCommenters()
                loadComments()
            }
        }

        // 댓글 영역 불러오기
        fun onShowCommentSheet() {
            _communityDetailViewState.update {
                it.copy(
                    showCommentSheet = true,
                    commentInput = "",
                    replyInput = "",
                    isReplyMode = false,
                    isSelectedReplyed = null,
                )
            }
        }

        fun closeShowCommentSheet() {
            _communityDetailViewState.update {
                it.copy(
                    showCommentSheet = false,
                    isEditMode = false,
                    editCommentId = null,
                    commentInput = "",
                )
            }
        }

        // ############################################################################
        // 사진 클릭 시 확대 보기 관련
        fun onImagePreview(uri: String) {
            _communityDetailViewState.update { it.copy(previewImage = uri) }
        }

        fun onImagePreviewDismissed() {
            _communityDetailViewState.update { it.copy(previewImage = "") }
        }

        // ############################################################################
        // 게시글 불러오기
        suspend fun loadCommunityDetail(id: Long) {
            if (id == 0L) return
            try {
                _communityDetailViewState.update { it.copy(isLoading = true) }

                // 1. 게시글 상세 정보 가져오기
                val result =
                    supabase
                        .from("community")
                        .select { filter { eq("id", id) } }
                        .decodeSingle<CommunityWriteModel>()

                // 2. 내가 이 게시글을 추천했는지 확인
                val userId = supabase.auth.currentUserOrNull()?.id
                val isLikedByMe =
                    if (userId != null) {
                        val likeCheck =
                            supabase
                                .from("community_likes")
                                .select {
                                    filter {
                                        eq("community_id", id)
                                        eq("user_id", userId)
                                    }
                                }.decodeSingleOrNull<CommunityLikeModel>()
                        likeCheck != null
                    } else {
                        false
                    }

                val likesResponse =
                    supabase
                        .from("community_likes")
                        .select {
                            filter { eq("community_id", id) }
                        }.decodeList<CommunityLikeModel>() // 리스트로 변환

                val totalLikes = likesResponse.size

                _communityDetailViewState.update {
                    it.copy(
                        communityDetail = result,
                        isLiked = isLikedByMe, // UI에 빨간 하트 표시 여부
                        isLoading = false,
                        likeCount = totalLikes,
                    )
                }
            } catch (e: Exception) {
                Log.e("Supabase", "데이터 조회 실패: ${e.message}")
            } finally {
                _communityDetailViewState.update { it.copy(isLoading = false) }
            }
        }

        // ############################################################################
        // 댓글 관련
        fun clickCommunityCommentBtn(
            inputComment: String,
            parentId: Long? = null,
        ) {
            if (inputComment.isEmpty()) return
            viewModelScope.launch {
                val user = userSession.getUserState()
//                val author = userSession.getUserState().name
                Log.d("CheckID", "유저 ID: ${user.id}")

                val newComment =
                    CommunityCommentModel(
                        // 게시글 ID
                        post_id = id,
                        // 유저 고유 ID
                        user_id = user.id,
                        // 유저 닉네임
                        author = user.name,
                        content = inputComment,
                        // 답글일 경우 부모 ID 전달
                        parent_id = parentId,
                    )

                insertComment(newComment)

                if (_communityDetailViewState.value.isReplyMode) {
                    val replyList = _communityDetailViewState.value.communityToalComments.filter { it.parent_id == parentId }
                    _communityDetailViewState.update {
                        it.copy(
                            replyInput = "",
                            replyList = replyList,
                        )
                    }
                }

                _communityDetailViewState.update {
                    it.copy(
                        commentInput = "",
                    )
                }
            }
        }

        suspend fun loadComments() {
            val myUserId = userSession.getUserState().id

            try {
                val rawComments =
                    supabase
                        .from("community_comments")
                        .select(
                            columns =
                                Columns.raw(
                                    """
                *,
                like_count:comment_likes(count),
                is_liked:comment_likes(user_id)
            """,
                                ),
                        ) {
                            filter { eq("post_id", id) } // 현재 게시글 ID로 필터링
                            order("created_at", order = Order.ASCENDING)
                        }.decodeList<CommunityCommentModel>()

                val totalComments =
                    rawComments.map { comment ->
                        comment.copy(
                            likeCount = comment.likeCountList.firstOrNull()?.count ?: 0,
                            isLiked = comment.likeUserList.any { it.user_id == myUserId },
                        )
                    }
//            val filterComments = rawComments
//                .filter { it.parent_id == null }
//                .map { comment ->
//                    comment.copy(
//                        likeCount = comment.likeCountList.firstOrNull()?.count ?: 0,
//                        isLiked = comment.likeUserList.any { it.user_id == myUserId }
//                    )
//                }
                val filterComments =
                    totalComments
                        .filter { it.parent_id == null }
                        .map { parent ->
                            val count = totalComments.count { it.parent_id == parent.id }
                            parent.copy(rePlyCount = count)
                        }

                _communityDetailViewState.update {
                    it.copy(
                        communityToalComments = totalComments,
                        communityComments = filterComments,
                    )
                }
//            Log.d("저장 후 답글1", filterComments.toString())
            } catch (e: Exception) {
                Log.e("Refresh Error", "댓글 불러오기 실패: ${e.message}")
            }
        }

        suspend fun loadTopCommenters() {
            try {
                val topCommenters =
                    supabase.postgrest
                        .rpc("get_top_commenters")
                        .decodeList<TopCommenter>()

                _communityDetailViewState.update { it.copy(topCommenters = topCommenters) }
            } catch (e: Exception) {
                Log.e("TopCommenter Error", e.toString())
            }
        }

        suspend fun insertComment(newComment: CommunityCommentModel) {
            try {
                supabase
                    .from("community_comments")
                    .insert(newComment)

                loadComments()
//            if(!_communityDetailViewState.value.isReplyMode){
//                loadComments()
//            }
            } catch (e: Exception) {
                Log.e("Comment Error", "댓글 저장 실패: ${e.message}")
            }
        }

        fun onValueChanged(comment: String) {
            _communityDetailViewState.update {
                it.copy(commentInput = comment)
            }
        }

        // ############################################################################
        // 답글 관련
        fun showCommentReply(commentId: Long) {
            val replyList = _communityDetailViewState.value.communityToalComments.filter { it.parent_id == commentId }
//        _communityDetailViewState.value.communityComments.forEach {
//        Log.d("댓글", it.toString())
//    }
//        Log.d("답글id", commentId.toString())
//        Log.d("답글", replyList.toString())
            val parentComment = _communityDetailViewState.value.communityComments.find { it.id == commentId }
//        Log.d("부모댓글", parentComment.toString())
            _communityDetailViewState.update {
                it.copy(
                    commentInput = "",
                    replyInput = "",
                    isSelectedReplyed = commentId,
                    isReplyMode = true,
                    replyList = replyList,
                    parentComment = parentComment,
                )
            }
        }

        fun closeCommentReply() {
            _communityDetailViewState.update {
                it.copy(
                    isEditMode = false,
                    isReplyMode = false,
                    isSelectedReplyed = null,
                    editCommentId = null,
                    replyInput = "",
                )
            }
        }

        fun onReplyValueChanged(replyInput: String) {
            _communityDetailViewState.update {
                it.copy(replyInput = replyInput)
            }
        }

        // ############################################################################
        // 게시글 좋아요 관련
        fun clickLike(id: Long) {
            viewModelScope.launch {
                onLike(id)
            }
        }

        suspend fun onLike(id: Long) {
            try {
                val currentUser = supabase.auth.currentUserOrNull() ?: return
                val newLike = CommunityLikeModel(community_id = id)

                try {
                    supabase.postgrest["community_likes"].insert(newLike)
                    Log.d("추천하기", "추천 저장 성공")

                    _communityDetailViewState.update {
                        it.copy(
                            isLiked = true,
                            likeCount = it.likeCount + 1,
                        )
                    }
                } catch (e: Exception) {
                    Log.d("추천하기", "이미 추천된 상태이므로 취소를 진행합니다.($e)")
                    supabase.postgrest["community_likes"].delete {
                        filter {
                            eq("community_id", id)
                            eq("user_id", currentUser.id)
                        }
                    }
                    Log.d("추천하기", "추천 취소 완료")

                    _communityDetailViewState.update {
                        it.copy(
                            isLiked = false,
                            likeCount = if (it.likeCount > 0) it.likeCount - 1 else 0,
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("게시글 추천하기 에러", e.toString())
            }
        }

        // ############################################################################
        // 댓글 좋아요 관련
        fun clickCommentLike(id: String) {
            viewModelScope.launch {
                onCommentLike(id)
            }
        }

        suspend fun onCommentLike(commentId: String) {
            val myUserId = userSession.getUserState().id

            try {
                val existingLike =
                    supabase
                        .from("comment_likes")
                        .select {
                            filter {
                                eq("comment_id", commentId)
                                eq("user_id", myUserId)
                            }
                        }.decodeSingleOrNull<LikeUserResponse>()

                if (existingLike != null) {
                    supabase
                        .from("comment_likes")
                        .delete {
                            filter {
                                eq("comment_id", commentId)
                                eq("user_id", myUserId)
                            }
                        }
                    Log.d("Like", "좋아요 취소 성공")
                } else {
                    val newLike =
                        mapOf(
                            "comment_id" to commentId,
                            "user_id" to myUserId,
                        )
                    supabase.from("comment_likes").insert(newLike)
                    Log.d("Like", "좋아요 등록 성공")
                }

                loadComments()
            } catch (e: Exception) {
                Log.e("댓글 추천하기 에러", "상세 이유: ${e.message}")
            }
        }
// ############################################################################

        // 공유하기 관련
        fun clickShare(
            context: Context,
            contents: CommunityWriteModel?,
        ) {
            viewModelScope.launch {
                onShare(
                    context = context,
                    contents = contents,
                )
            }
        }

        fun onShare(
            context: Context,
            contents: CommunityWriteModel?,
        ) {
            if (contents == null) return
            try {
                _communityDetailViewState.update {
                    it.copy(
                        isLoading = true,
                    )
                }

                if (ShareClient.instance.isKakaoTalkSharingAvailable(context)) {
                    val firstImage = contents.images.firstOrNull()

                    val feedTemplate =
                        FeedTemplate(
                            content =
                                Content(
                                    title = contents.title,
                                    description = contents.content,
                                    imageUrl = firstImage ?: "https://your-image-url.com/logo.png",
                                    link =
                                        Link(
                                            /*
                                                1. 공유 시 postId = "해당 id"  전달
                                                2. 앱에서 클릭 시 해당 번호를 보고 기존 앱으로 이동
                                             */
                                            androidExecutionParams = mapOf("postId" to contents.id.toString()), // ✅ 핵심
                                            iosExecutionParams = mapOf("postId" to contents.id.toString()),
                                            // 주소 이동 활성화를 위한 더미 주소
                                            mobileWebUrl = "https://com.easylaw.app",
                                            webUrl = "https://com.easylaw.app",
                                        ),
                                ),
                            buttons =
                                listOf(
                                    Button(
                                        "앱에서 보기",
                                        Link(
                                            androidExecutionParams = mapOf("postId" to contents.id.toString()),
                                            iosExecutionParams = mapOf("postId" to contents.id.toString()),
                                            mobileWebUrl = "https://com.easylaw.app",
                                            webUrl = "https://com.easylaw.app",
                                        ),
                                    ),
                                ),
                        )
                    ShareClient.instance.shareDefault(context, feedTemplate) { sharingResult, error ->
                        _communityDetailViewState.update {
                            it.copy(
                                isLoading = false,
                            )
                        }
                        if (error != null) {
                            Log.e("onShare error", "공유 실패", error)
                        } else if (sharingResult != null) {
                            context.startActivity(sharingResult.intent)
                        }
                    }
                } else {
                    _communityDetailViewState.update { it.copy(isLoading = false) }
                    val sendIntent =
                        Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "[${contents.title}]\n${contents.content}\nhttps://easylaw.com/community/${contents.id}")
                            type = "text/plain"
                        }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                }
            } catch (e: Exception) {
                _communityDetailViewState.update {
                    it.copy(
                        isLoading = false,
                    )
                }
                Log.e("onShare error", e.toString())
            }
        }
// ############################################################################
// 수정/삭제 관련

        fun toggleOpenMore(id: Long) {
            _communityDetailViewState.update {
                it.copy(
                    isOpenMoreSelected = if (it.isOpenMoreSelected == id) null else id,
                )
            }
            Log.d("답글", "isOpenMoreSelected 2: ${_communityDetailViewState.value.isOpenMoreSelected} , id : $id")
        }

        fun commentEdit(
            id: Long,
            content: String,
            isReplyMode: Boolean = false,
        ) {
//        Log.d("수정", "id: ${id} , content: ${content}")
            if (!isReplyMode) {
                Log.d("댓글수정", "id: $id , content: $content")
                _communityDetailViewState.update {
                    it.copy(
                        isEditMode = true,
                        commentInput = content,
                        isOpenMoreSelected = null,
                        editCommentId = id,
                    )
                }
            } else {
                Log.d("답글수정", "id: $id , content: $content")
                _communityDetailViewState.update {
                    it.copy(
                        isEditMode = true,
                        replyInput = content,
                        isOpenMoreSelected = null,
                        editCommentId = id,
                    )
                }
            }
        }
//    fun replyEdit(id: Long, content: String) {
// //        Log.d("수정", "id: ${id} , content: ${content}")
//        _communityDetailViewState.update {
//            it.copy(
//                isEditMode = true,
//                replyInput = content,
//                isOpenMoreSelected = null,
//                editCommentId = id
//            )
//        }
//
//    }

        fun updateComment(
            id: Long?,
            content: String,
            parentId: Long? = null,
        ) {
            Log.d("수정", "id: $id , content: $content")
            if (id == null || content.isBlank()) return

            viewModelScope.launch {
                try {
                    supabase.from("community_comments").update(
                        {
                            set("content", content)
                        },
                    ) {
                        filter {
                            eq("id", id)
                        }
                    }

                    _communityDetailViewState.update {
                        it.copy(
                            isEditMode = false,
                            editCommentId = null,
                            commentInput = "",
                        )
                    }

                    loadComments()

                    if (_communityDetailViewState.value.isReplyMode) {
                        val replyList = _communityDetailViewState.value.communityToalComments.filter { it.parent_id == parentId }
                        _communityDetailViewState.update {
                            it.copy(
                                replyInput = "",
                                replyList = replyList,
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Update Error", "댓글 수정 실패: ${e.message}")
                }
            }
        }

        fun communityDelete() {
            _communityDetailViewState.update {
                it.copy(
                    isCommunityDeleteMode = true,
                )
            }
        }

        fun commentDelete(id: Long) {
            _communityDetailViewState.update {
                it.copy(
                    isDeleteMode = true,
                    isOpenMoreSelected = null,
                    deleteCommentId = id,
                )
            }
        }

        fun deleteComment(
            id: Long,
            isReplyMode: Boolean = false,
            parentId: Long? = null,
        ) {
            viewModelScope.launch {
                try {
                    supabase
                        .from("community_comments")
                        .delete {
                            filter {
                                eq("id", id)
                            }
                        }

                    _communityDetailViewState.update {
                        it.copy(
                            isEditMode = false,
                            editCommentId = null,
                            commentInput = "",
                            isOpenMoreSelected = null,
                        )
                    }

                    loadComments()

                    if (isReplyMode) {
                        val replyList = _communityDetailViewState.value.communityToalComments.filter { it.parent_id == parentId }
                        _communityDetailViewState.update {
                            it.copy(
                                replyInput = "",
                                replyList = replyList,
                            )
                        }
                    }

                    cancelDeleteMode()
                } catch (e: Exception) {
                    Log.e("Delete Error", "댓글 삭제 실패: ${e.message}")
                }
            }
        }

        fun onDeleteValueChanged(text: String) {
            _communityDetailViewState.update {
                it.copy(
                    deleteInputText = text,
                )
            }
        }

        fun cancelDeleteMode() {
            _communityDetailViewState.update {
                it.copy(
                    isDeleteMode = false,
                    deleteCommentId = null,
                    deleteInputText = "",
                )
            }
        }

        fun onCommunityDeleteValueChanged(text: String) {
            _communityDetailViewState.update {
                it.copy(
                    deleteCommunityInputText = text,
                )
            }
        }

        fun cancelCommunityDeleteMode() {
            _communityDetailViewState.update {
                it.copy(
                    isCommunityDeleteMode = false,
                    deleteCommunityInputText = "",
                )
            }
        }

        fun deleteCommunity(id: Long) {
//        Log.d("DeleteTest", "삭제 시도 ID: $id")
            viewModelScope.launch {
                try {
                    val result =
                        supabase
                            .from("community")
                            .delete {
                                filter {
                                    eq("id", id)
                                }
                            }
//                Log.d("DeleteTest", "서버 응답 결과: $result")

                    cancelCommunityDeleteMode()
                } catch (e: Exception) {
                    Log.e("DeleteTest", "삭제 중 예외 발생: ${e.message}")
                } finally {
//                _communityDetailViewState.update {
//                    it.copy(isCommunityDeleted = true)
//                }
                    _isDeleteSuccess.send(Unit)
                }
            }
        }
    }
