package com.easylaw.app.ui.screen.community

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import coil.compose.AsyncImage
import com.easylaw.app.data.models.CommunityCommentModel
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonPreview
import com.easylaw.app.util.Common
import com.easylaw.app.viewmodel.CommunityDetailViewModel
import com.easylaw.app.viewmodel.CommunityDetailViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityDetailView(
    modifier: Modifier = Modifier,
    viewModel: CommunityDetailViewModel,
    goBack: () -> Unit,
    goUpdate: (Long) -> Unit,
) {
    val viewState by viewModel.communityDetailViewState.collectAsState()
    val scrollState = rememberScrollState()
    val contents = viewState.communityDetail
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.isDeleteSuccess.collect { goBack() }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshCommunityDetail()
    }

    BackHandler {
        if (viewState.isReadOnly) (context as? Activity)?.finish() else goBack()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB)), // 앱 공통 배경색
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .statusBarsPadding()
                                .height(56.dp)
                                .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!viewState.isReadOnly) {
                            IconButton(onClick = { goBack() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFF191F28))
                            }
                        }
                        Text(
                            text = "게시글 상세보기",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28)),
                        )
                    }
                }
            },
        ) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(scrollState),
            ) {
                if (contents != null) {
                    // --- 본문 메인 카드 ---
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFEFF1F3)),
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            DetailCategorySection(
                                contents.category,
                                userId = viewState.userId,
                                communityUserId = contents.user_id ?: "",
                                goUpdate = { goUpdate(contents.id ?: 0L) },
                                communityDelete = { viewModel.communityDelete() },
                                isReadOnly = viewState.isReadOnly,
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DetailTitleInfoSection(
                                title = contents.title,
                                author = contents.author,
                                created = contents.created_at,
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 24.dp),
                                color = Color(0xFFF2F4F6),
                                thickness = 1.dp,
                            )

                            DetailContentSection(content = contents.content)

                            if (contents.images.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                DetailImageSection(
                                    images = contents.images,
                                    onImagePreview = { viewModel.onImagePreview(it) },
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            DetailLikeShareSection(
                                clickLike = { if (!viewState.isReadOnly) viewModel.clickLike(contents.id ?: 0L) },
                                isLiked = viewState.isLiked,
                                likeCount = viewState.likeCount,
                                clickShare = { if (!viewState.isReadOnly) viewModel.clickShare(context, contents) },
                            )
                        }
                    }

                    // --- 댓글 보기 플로팅 바 ---
                    Surface(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { viewModel.onShowCommentSheet() },
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, Color(0xFFEFF1F3)),
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF3182F6),
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "댓글 ${viewState.communityComments.size}개 보기",
                                    style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4E5968)),
                                )
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB0B8C1))
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // --- BottomSheet (댓글/답글) ---
        if (viewState.showCommentSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeShowCommentSheet() },
                sheetState = sheetState,
                containerColor = Color.White,
                dragHandle = {
                    Surface(
                        modifier = Modifier.padding(vertical = 12.dp).width(40.dp).height(4.dp),
                        color = Color(0xFFD1D6DB),
                        shape = RoundedCornerShape(2.dp),
                    ) {}
                },
            ) {
                BackHandler(enabled = viewState.isReplyMode) { viewModel.closeCommentReply() }

                // [댓글/답글 통합 레이아웃]
                CommentSheetContent(
                    viewState = viewState,
                    viewModel = viewModel,
                )
            }
        }

        // --- 삭제 확인 다이얼로그 (댓글/게시글 공통 스타일) ---
        if (viewState.isDeleteMode || viewState.isCommunityDeleteMode) {
            TossDeleteDialog(
                title = if (viewState.isDeleteMode) "댓글을 삭제할까요?" else "게시글을 삭제할까요?",
                inputText = if (viewState.isDeleteMode) viewState.deleteInputText else viewState.deleteCommunityInputText,
                onValueChange = { if (viewState.isDeleteMode) viewModel.onDeleteValueChanged(it) else viewModel.onCommunityDeleteValueChanged(it) },
                onCancel = { if (viewState.isDeleteMode) viewModel.cancelDeleteMode() else viewModel.cancelCommunityDeleteMode() },
                onConfirm = {
                    if (viewState.isDeleteMode) {
                        viewState.deleteCommentId?.let { id -> viewModel.deleteComment(id, viewState.isReplyMode, viewState.isSelectedReplyed) }
                    } else {
                        viewModel.deleteCommunity(viewState.communityDetail?.id ?: 0L)
                    }
                },
            )
        }

        // --- 기타 오버레이 ---
        if (viewState.previewImage?.isNotEmpty() == true) {
            CommonPreview(previewImage = viewState.previewImage ?: "", clickable = { viewModel.onImagePreviewDismissed() })
        }
        if (viewState.isLoading) CommonIndicator(title = "잠시만 기다려주세요...")
    }
}

@Composable
fun DetailCategorySection(
    category: String,
    userId: String,
    goUpdate: () -> Unit,
    communityUserId: String,
    communityDelete: () -> Unit,
    isReadOnly: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = Color(0xFFE8F3FF),
            shape = RoundedCornerShape(6.dp),
        ) {
            Text(
                text = category,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF3182F6),
            )
        }
        if (!isReadOnly && userId == communityUserId) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                // 적절한 여백
                horizontalArrangement = Arrangement.End, // 우측 끝으로 밀기
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "수정",
                    style =
                        TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold, // 약간 두껍게
                            color = Color(0xFF3182F6), // 토스 대표 블루색
                        ),
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp)) // 클릭 영역도 둥글게
                            .clickable {
                                goUpdate()
                            }.padding(horizontal = 12.dp, vertical = 8.dp), // 터치 영역 확보
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier =
                        Modifier
                            .width(1.dp)
                            .height(14.dp)
                            .background(Color(0xFFE5E8EB)),
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "삭제",
                    style =
                        TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF04452),
                        ),
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                communityDelete()
                            }.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
fun DetailAuthorCreatedRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF4E5968),
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(60.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8B95A1),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
        )
    }
}

@Composable
fun DetailTitleInfoSection(
    title: String,
    author: String,
    created: String,
) {
    Text(
        text = title,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        lineHeight = 36.sp,
        color = Color(0xFF1B2128),
    )

    Spacer(modifier = Modifier.height(32.dp))

    // 작성자, 작성일
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF2F4F6),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            DetailAuthorCreatedRow(
                label = "작성자",
                value = if (author.isNotEmpty()) "$author 님" else "익명",
                valueColor = Color(0xFF3182F6),
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailAuthorCreatedRow(
                label = "작성일",
                value = Common.formatIsoDate(created),
            )
        }
    }
}

@Composable
fun DetailContentSection(content: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .background(Color(0xFF3182F6), RoundedCornerShape(2.dp)),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "본문 내용",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191F28),
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF2F4F6),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE5E8EB)),
    ) {
        Text(
            text = content,
            modifier =
                Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
            fontSize = 17.sp,
            lineHeight = 28.sp,
            color = Color(0xFF333D4B),
        )
    }
}

@Composable
fun DetailImageSection(
    images: List<String>,
    onImagePreview: (String) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF9FAFB))
                .padding(16.dp),
    ) {
        Column {
            Text(
                text = "첨부된 사진 ${images.size}/3",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B95A1),
                modifier = Modifier.padding(bottom = 12.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(images) { imageUri ->
                    Box(
                        modifier =
                            Modifier
                                .size(56.dp)
                                .clickable { onImagePreview(imageUri) }
                                .background(Color.White, RoundedCornerShape(12.dp)),
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            onSuccess = { Log.d("Coil", "로드 성공: $imageUri") },
                            onError = { error -> Log.e("Coil", "로드 실패: ${error.result.throwable.message}") }, // 여기서 에러 확인!
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailLikeShareSection(
    isLiked: Boolean,
    likeCount: Int,
    clickLike: (() -> Unit)? = null,
    clickShare: (() -> Unit)? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .clickable {
                        clickLike?.invoke()
                    },
            contentAlignment = Alignment.Center,
        ) {
            DetailLikeShare(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                label = "추천하기",
                tint = if (isLiked) Color.Red else Color.Gray,
                likeCount = likeCount,
                isLiked = isLiked,
            )
        }

        Box(
            modifier =
                Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(Color(0xFFF2F4F6)),
        )

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .clickable {
                        clickShare?.invoke()
                    },
            contentAlignment = Alignment.Center,
        ) {
            DetailLikeShare(
                icon = Icons.Default.Share,
                label = "공유하기",
                tint = Color(0xFF3182F6),
            )
        }
    }
}

@Composable
fun DetailLikeShare(
    icon: ImageVector,
    label: String,
    tint: Color,
    likeCount: Int = 0,
    isLiked: Boolean = false,
) {
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        label = "SizeAnimation",
    )

    val animatedTint by animateColorAsState(
        targetValue = tint,
        animationSpec = tween(durationMillis = 300),
        label = "ColorAnimation",
    )

    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = animatedTint, // 애니메이션되는 색상 적용
            modifier =
                Modifier
                    .size(28.dp)
                    .graphicsLayer(
                        // 크기 애니메이션 적용
                        scaleX = scale,
                        scaleY = scale,
                    ),
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (label == "추천하기") {
            Text(
                text = "$likeCount",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E5968),
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4E5968),
        )
    }
}

@Composable
fun CommentItem(
    currentUserId: String,
    comment: CommunityCommentModel,
    isReadOnly: Boolean = false,
    clickCommentLike: ((String) -> Unit)? = null,
    showCommentReply: ((Long) -> Unit)? = null,
    isReplyMode: Boolean = false,
    toggleOpenMore: (Long) -> Unit,
    isOpenMoreSelected: Long? = null,
    commentEdit: ((Long, String, Boolean) -> Unit)? = null,
    commentDelete: ((Long) -> Unit)? = null,
    isParentReply: Boolean = false,
    commenterRank: Int? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFE5E8EB),
            modifier = Modifier.size(36.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = comment.author.take(1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E5968),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 작성자 이름
                Text(
                    text = comment.author,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                )
                if (commenterRank != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color =
                            when (commenterRank) {
                                1 -> Color(0xFFFFEFC3) // 1등은 금색 느낌
                                else -> Color(0xFFE8F3FF) // 2, 3등은 하늘색
                            },
                        shape = RoundedCornerShape(4.dp),
                    ) {
                        Text(
                            text =
                                when (commenterRank) {
                                    1 -> "🏆 명예 해결사"
                                    2 -> "🥈 커뮤니티 멘토"
                                    3 -> "🥉 열정 답변러"
                                    else -> ""
                                },
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                when (commenterRank) {
                                    1 -> Color(0xFFF2A100)
                                    else -> Color(0xFF3182F6)
                                },
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                comment.created_at?.let {
                    Text(
                        text = Common.formatIsoDate(it),
                        fontSize = 11.sp,
                        color = Color(0xFF8B95A1),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                if (!isReadOnly && currentUserId == comment.user_id && !isParentReply) {
                    Box {
                        IconButton(
                            onClick = {
//                                Log.d("답글", "isOpenMoreSelected 1: ${isOpenMoreSelected} , id : ${comment.id}")
                                comment.id?.let {
                                    toggleOpenMore(
                                        comment.id,
                                    )
                                }
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기",
                                tint = Color(0xFFADB5BD),
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = isOpenMoreSelected == comment.id,
                            onDismissRequest = {
                                comment.id?.let {
                                    toggleOpenMore(
                                        comment.id,
                                    )
                                }
                            }, // 바깥 클릭 시 닫기
                            modifier = Modifier.background(Color.White),
                        ) {
                            DropdownMenuItem(
                                text = { Text("수정", fontSize = 14.sp) },
                                onClick = {
                                    comment.id?.let {
//                                        viewModel.startEditMode(it, comment.content)
                                        commentEdit?.invoke(comment.id, comment.content, isReplyMode)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            )

                            DropdownMenuItem(
                                text = { Text("삭제", fontSize = 14.sp, color = Color.Red) },
                                onClick = {
//                                    comment.id?.let { commentDelete?.invoke(comment.id, isReplyMode,isParentId ) }
                                    if (comment.id != null) {
                                        commentDelete?.invoke(comment.id)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) },
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = comment.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF333D4B),
            )
            if (!isReplyMode) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier.clickable {
                                clickCommentLike?.invoke(comment.id.toString())
                            },
                    ) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (comment.isLiked) Color.Red else Color.Gray,
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${comment.likeCount}",
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Row {
                        Box(
                            modifier =
                                Modifier.clickable {
                                    comment.id?.let { showCommentReply?.invoke(it) }
                                },
                        ) {
                            Text(text = "답글", fontSize = 12.sp, color = Color(0xFF4E5968))
                        }
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (comment.rePlyCount > 0) "${comment.rePlyCount}" else "0",
                            fontSize = 12.sp,
                            color = Color(0xFF8B95A1),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityTextField(
    commentInput: String,
    onValueChanged: (String) -> Unit,
    clickCommunityCommentBtn: () -> Unit,
    enabled: Boolean,
    placeholder: String,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

//    BackHandler {
//        keyboardController?.hide()
//        focusManager.clearFocus()
//    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 16.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = commentInput,
                onValueChange = { onValueChanged(it) },
                placeholder = { Text(placeholder, fontSize = 14.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF3182F6),
                        unfocusedBorderColor = Color(0xFFE5E8EB),
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                    ),
                maxLines = 3,
            )

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = {
                    clickCommunityCommentBtn()
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "전송",
                    tint = if (enabled) Color(0xFF3182F6) else Color(0xFFD1D6DB),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TossDeleteDialog(
    title: String,
    inputText: String,
    onValueChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    BasicAlertDialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28)))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "정말 삭제하시겠습니까?\n확인을 위해 '삭제'라고 입력해주세요.", textAlign = TextAlign.Center, color = Color(0xFF4E5968), fontSize = 15.sp)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onValueChange,
                    placeholder = { Text("'삭제' 입력", color = Color(0xFFB0B8C1)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF3182F6), unfocusedBorderColor = Color(0xFFE5E8EB)),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F4F6), contentColor = Color(0xFF4E5968)),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("취소") }
                    Button(
                        enabled = inputText == "삭제",
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF04452), contentColor = Color.White, disabledContainerColor = Color(0xFFE5E8EB)),
                        shape = RoundedCornerShape(14.dp),
                    ) { Text("삭제하기") }
                }
            }
        }
    }
}

@Composable
fun CommentSheetContent(
    viewState: CommunityDetailViewState,
    viewModel: CommunityDetailViewModel,
) {
    val isReplyMode = viewState.isReplyMode

    Column(
        modifier =
            Modifier
                .fillMaxHeight(0.9f)
                .navigationBarsPadding()
                .imePadding(),
    ) {
        // 1. 헤더 (댓글일 때와 답글일 때 타이틀 변경)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isReplyMode) {
                IconButton(onClick = { viewModel.closeCommentReply() }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "뒤로가기", tint = Color(0xFF191F28))
                }
            }
            Text(
                text = if (isReplyMode) "답글" else "댓글",
                modifier = Modifier.padding(start = if (isReplyMode) 0.dp else 20.dp),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28)),
            )
        }

        HorizontalDivider(color = Color(0xFFF2F4F6), thickness = 1.dp)

        // 2. 리스트 영역
        val topCommenterMap =
            remember(viewState.topCommenters) {
                viewState.topCommenters.associate { it.id to (viewState.topCommenters.indexOf(it) + 1) }
            }

        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
        ) {
            if (isReplyMode) {
                // [답글 모드] 부모 댓글 상단 고정
                viewState.parentComment?.let { parent ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        CommentItem(
                            currentUserId = viewState.userId,
                            comment = parent,
                            isReplyMode = true,
                            toggleOpenMore = { viewModel.toggleOpenMore(it) },
                            isParentReply = true,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFF9FAFB), thickness = 8.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // 답글 목록
                if (viewState.replyList.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                            Text("아직 답글이 없습니다.", color = Color(0xFF8B95A1), fontSize = 14.sp)
                        }
                    }
                } else {
                    items(viewState.replyList) { reply ->
                        CommentItem(
                            currentUserId = viewState.userId,
                            comment = reply,
                            isReplyMode = true,
                            toggleOpenMore = { viewModel.toggleOpenMore(it) },
                            isOpenMoreSelected = viewState.isOpenMoreSelected,
                            commentEdit = { id, content, isReply -> viewModel.commentEdit(id, content, isReply) },
                            commentDelete = { viewModel.commentDelete(it) },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // [댓글 모드] 일반 댓글 목록
                items(viewState.communityComments.reversed()) { comment ->
                    CommentItem(
                        currentUserId = viewState.userId,
                        comment = comment,
                        isReadOnly = viewState.isReadOnly,
                        clickCommentLike = { if (!viewState.isReadOnly) viewModel.clickCommentLike(it) },
                        showCommentReply = { if (!viewState.isReadOnly) viewModel.showCommentReply(it) },
                        toggleOpenMore = { viewModel.toggleOpenMore(it) },
                        isOpenMoreSelected = viewState.isOpenMoreSelected,
                        commentEdit = { id, content, _ -> viewModel.commentEdit(id, content) },
                        commentDelete = { viewModel.commentDelete(it) },
                        commenterRank = topCommenterMap[comment.user_id],
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 3. 입력창 영역
        if (!viewState.isReadOnly) {
            CommunityTextField(
                commentInput = if (isReplyMode) viewState.replyInput else viewState.commentInput,
                onValueChanged = { if (isReplyMode) viewModel.onReplyValueChanged(it) else viewModel.onValueChanged(it) },
                clickCommunityCommentBtn = {
                    if (!viewState.isEditMode) {
                        viewModel.clickCommunityCommentBtn(
                            inputComment = if (isReplyMode) viewState.replyInput else viewState.commentInput,
                            parentId = if (isReplyMode) viewState.isSelectedReplyed else null,
                        )
                    } else {
                        viewModel.updateComment(
                            id = viewState.editCommentId,
                            content = if (isReplyMode) viewState.replyInput else viewState.commentInput,
                            parentId = if (isReplyMode) viewState.isSelectedReplyed else null,
                        )
                    }
                },
                enabled = (if (isReplyMode) viewState.replyInput else viewState.commentInput).isNotBlank(),
                placeholder =
                    if (viewState.isEditMode) {
                        "수정할 내용을 입력하세요..."
                    } else if (isReplyMode) {
                        "답글 추가..."
                    } else {
                        "댓글 추가..."
                    },
            )
        }
    }
}
