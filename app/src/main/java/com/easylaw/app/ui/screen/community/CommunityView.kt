package com.easylaw.app.ui.screen.community

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.easylaw.app.data.models.CommunityNewsModel
import com.easylaw.app.data.models.CommunityPrecModel
import com.easylaw.app.data.models.CommunityWriteModel
import com.easylaw.app.data.models.NaverNewsModel
import com.easylaw.app.data.models.NewsItem
import com.easylaw.app.domain.model.TopCommenter
import com.easylaw.app.ui.components.CommonDialog
import com.easylaw.app.ui.components.CommonFilterCategory
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.util.Common
import com.easylaw.app.viewmodel.CommunityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityView(
    modifier: Modifier,
    viewModel: CommunityViewModel,
    communityWrite: () -> Unit,
    gotoDetail: (String) -> Unit,
) {
    val viewState by viewModel.communityState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshCommunityList()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    Color(
                        0xFFF9FAFB,
                    ),
                ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "커뮤니티",
                                style =
                                    TextStyle(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF191F28),
                                        letterSpacing = (-0.5).sp,
                                        shadow =
                                            Shadow(
                                                color = Color.Black.copy(alpha = 0.05f),
                                                offset = Offset(2f, 2f),
                                                blurRadius = 4f,
                                            ),
                                    ),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF3182F6).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(100.dp),
                            ) {
                                Text(
                                    text = "게시판",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3182F6),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "나와 비슷한 상황의 사람들과\n이야기를 나눠보세요.",
                            style =
                                TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = Color(0xFF4E5968),
                                    fontWeight = FontWeight.Medium,
                                ),
                        )
                    }

                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 6.dp,
                        border = BorderStroke(1.dp, Color(0xFFF2F4F6)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Groups, // 또는 대화 아이콘
                                contentDescription = null,
                                tint = Color(0xFF3182F6),
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                CommonFilterCategory(
                    category = viewState.categoryList,
                    selectedCategory = viewState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) },
                )
            }
            if (!viewState.showCounselor) {
                communityTopSection(
                    viewState.keywords,
                    viewState.aiErrorText,
                    onBottomSheet = { keyword ->
                        viewModel.onBottomSheet(keyword)
                    },
                    showCounselorList = {
                        viewModel.showCounselorList()
                    },
//                showCounselor = viewState.showCounselor,
                    topCommenter = viewState.topCommenters,
                    communityNewList = viewState.communityNewList,
                )
            }
            PullToRefreshBox(
                isRefreshing = viewState.isCommunityListLoading,
                onRefresh = { viewModel.onCategorySelected(viewState.selectedCategory) },
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp)) {
                    items(viewState.communityList) { item ->
                        CommunityPostItemCard(
                            item = item,
                            gotoDetail = gotoDetail,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = viewState.showCounselor,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        ) {
            // 이 안에 뉴스, 판례, 샐럽이 다 들어감
            ExpandedDashboardOverlay(
                communityNewList = viewState.communityNewList,
                topCommenter = viewState.topCommenters,
                communityPrecList = viewState.communityPrecList,
                onClose = { viewModel.showCounselorList() },
                onNewsClick = { viewModel.onBottomSheet(it) },
            )
        }

        FloatingActionButton(
            onClick = { communityWrite() },
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White,
            shape = CircleShape,
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "글쓰기")
        }
        if (viewState.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeBottomSheet() }, // 시트 닫기 요청 시 상태 변경
                sheetState = sheetState,
            ) {
                NewsListBottomSheetContent(
                    naverNewsItem = viewState.naverNewsItem,
                )
            }
        }
        if (viewState.isCommunityListLoading) {
            CommonIndicator(title = "잠시만 기다려주세요...")
        }
        if (viewState.aiErrorText.isNotEmpty()) {
            CommonDialog(
                title = "네이버 응답 에러",
                desc = viewState.aiErrorText,
                icon = Icons.Default.Close,
                onConfirm = { viewModel.closeShowDialog() },
            )
        }
    }
}

@Composable
fun communityTopSection(
    keyworkds: List<String>,
    aiErrorText: String,
    onBottomSheet: (String) -> Unit,
    showCounselorList: () -> Unit,
    showCounselor: Boolean = false,
    topCommenter: List<TopCommenter>,
    communityNewList: List<CommunityNewsModel>,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
        color = Color(0xFFF2F4F6),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🤖  정보 광장",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                )
                Text(
                    text = if (!showCounselor) "전체보기" else "닫기",
                    fontSize = 13.sp,
                    color = Color(0xFF8B95A1),
                    modifier =
                        Modifier.clickable {
                            showCounselorList()
                        },
                )
            }
        }
    }
}

@Composable
fun CommunityPostItemCard(
    item: CommunityWriteModel,
    gotoDetail: (String) -> Unit,
) {
    // 게시글 하나하나를 입체적인 카드로 변경
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .clickable { gotoDetail(item.id.toString()) },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFFEFF1F3)),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 상단 정보 (카테고리, 작성자)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFF2F8FF),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = item.category,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3182F6),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (item.author.isNotEmpty()) "${item.author} 님" else "익명",
                    fontSize = 12.sp,
                    color = Color(0xFF8B95A1),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = Common.formatIsoDate(item.created_at),
                    fontSize = 11.sp,
                    color = Color(0xFFB0B8C1),
                )
            }

            Text(
                text = item.title,
                modifier = Modifier.padding(top = 12.dp),
                style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28)),
            )

            Text(
                text = item.content,
                modifier = Modifier.padding(top = 6.dp),
                style = TextStyle(fontSize = 14.sp, color = Color(0xFF4E5968), lineHeight = 20.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF2F4F6))

            // 하단 반응 정보 (좋아요, 댓글)
            Row(verticalAlignment = Alignment.CenterVertically) {
                ReactionBadge(
                    icon = Icons.Default.Favorite,
                    count = item.likeCount,
                    activeColor = Color(0xFFFF4D4D),
                    isActive = item.likeCount > 0,
                )
                Spacer(modifier = Modifier.width(12.dp))
                ReactionBadge(
                    icon = if (item.commentCount >= 3) Icons.Default.Whatshot else Icons.Outlined.ChatBubbleOutline,
                    count = item.commentCount,
                    activeColor = Color(0xFFFF5F2E),
                    isActive = item.commentCount >= 3,
                )
            }
        }
    }
}

@Composable
fun ReactionBadge(
    icon: ImageVector,
    count: Int,
    activeColor: Color,
    isActive: Boolean,
) {
    val color = if (isActive) activeColor else Color(0xFF8B95A1)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "$count", fontSize = 13.sp, color = color, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummarySection(
    communityNewList: List<CommunityNewsModel>,
    onBottomSheet: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 16.dp),
        // 상하 여백으로 입체감 확보
        horizontalArrangement = Arrangement.spacedBy(8.dp), // 간격을 padding 대신 배치로 조절
    ) {
        communityNewList.forEach { communityNewList ->
            Surface(
                onClick = { onBottomSheet(communityNewList.title) },
                shape = RoundedCornerShape(100.dp),
                color = Color(0xFFF2F8FF), // 아주 연한 푸른빛 배경
                border = BorderStroke(1.dp, Color(0xFFD0E3FF)), // 부드러운 푸른색 테두리
                shadowElevation = 2.dp, // 🌟 미세한 그림자로 "떠 있는" 느낌 추가
                modifier = Modifier.height(40.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    // 반짝이는 효과를 주는 아이콘으로 변경
                    Icon(
                        imageVector = Icons.Default.DoubleArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF3182F6),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = communityNewList.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B64D1), // 조금 더 깊은 블루
                        letterSpacing = (-0.3).sp, // 자간을 좁혀 응축된 느낌
                    )
                }
            }
        }
    }
}

@Composable
fun NewsListBottomSheetContent(naverNewsItem: NaverNewsModel) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .heightIn(min = 400.dp, max = 600.dp), // 적절한 높이 제한
    ) {
        Text(
            text = "추천 뉴스",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
        )

        if (naverNewsItem.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("관련 뉴스를 찾을 수 없습니다.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(naverNewsItem.items) { news ->
                    NewsItemRow(news)
                    HorizontalDivider(color = Color(0xFFF2F4F6), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun NewsItemRow(news: NewsItem) {
    val context = LocalContext.current
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
//                    Log.d("news", news.toString())
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("news", "링크를 열 수 없습니다: ${news.link}", e)
                    }
                },
    ) {
        Text(
            text = news.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191F28),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = news.description,
            fontSize = 14.sp,
            color = Color(0xFF4E5968),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = news.pubDate,
            fontSize = 12.sp,
            color = Color(0xFF8B95A1),
        )
    }
}

@Composable
fun CounselorCard(
    comment: TopCommenter,
    rank: Int,
) {
    Surface(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFF2F4F6)),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(Color(0xFFF2F8FF), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                when (rank) {
                    1 -> Text(text = "🏆", fontSize = 24.sp)
                    2 -> Text(text = "🥈", fontSize = 24.sp)
                    3 -> Text(text = "🥉", fontSize = 24.sp)
                    else -> {
                        Text(
                            text = comment.name.take(1),
                            color = Color(0xFF3182F6),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = comment.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Text(
                text = "답변 ${comment.commentCount} 회",
                fontSize = 11.sp,
                color = Color(0xFF3182F6),
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ExpandedDashboardOverlay(
    communityNewList: List<CommunityNewsModel>,
    topCommenter: List<TopCommenter>,
    communityPrecList: List<CommunityPrecModel>, // 판례 추가
    onClose: () -> Unit,
    onNewsClick: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF2F4F6), // 기존 배경색 유지
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding() // 상태바 겹침 방지
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "오늘의 브리핑", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "닫기",
                    color = Color.Gray,
                    modifier = Modifier.clickable { onClose() },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. 뉴스 섹션
            Text(text = "🤖 오늘의 뉴스", fontWeight = FontWeight.Bold)
            SummarySection(communityNewList, onBottomSheet = onNewsClick)

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "⚖️ 관련 판례", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            if (communityPrecList.isEmpty()) {
                Text(text = "검색된 판례가 없습니다.", color = Color.Gray, fontSize = 13.sp)
            } else {
                Box(modifier = Modifier.heightIn(max = 350.dp)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(communityPrecList) { prec ->
                            LawSummaryCard(prec = prec)
                        }
                    }
                }
//                communityPrecList.forEach { prec ->
//                    LawSummaryCard(prec = prec)
//                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. 샐럽 섹션
            Text(text = "🔥 커뮤니티 샐럽", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                topCommenter.forEachIndexed { index, comment ->
                    CounselorCard(comment = comment, rank = index + 1)
                }
            }
        }
    }
}

@Composable
fun LawSummaryCard(prec: CommunityPrecModel) {
    val context = LocalContext.current
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable {
//                Log.d("판례 상세 주소", "${prec.detailLink.toString()}")
                    val fullUrl =
                        if (prec.detailLink.startsWith("/")) {
                            "https://www.law.go.kr${prec.detailLink}"
                        } else {
                            prec.detailLink
                        }

                    // 2. 완성된 주소로 Intent 실행
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                    context.startActivity(intent)
                },
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = prec.caseTypeName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        modifier =
                            Modifier
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                    Text(
                        text = prec.judgmentType,
                        fontSize = 11.sp,
                        color = Color.Gray,
                    )
                }
                // 선고일자
                Text(
                    text = prec.sentenceDate,
                    fontSize = 11.sp,
                    color = Color.Gray,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 사건명 (가공 없이 그대로 노출)
            Text(
                text = prec.caseName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF191F28),
                lineHeight = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 법원명 및 사건번호
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${prec.courtName} · ${prec.caseNumber}",
                    fontSize = 12.sp,
                    color = Color(0xFF8B95A1),
                )
            }
        }
    }
}
