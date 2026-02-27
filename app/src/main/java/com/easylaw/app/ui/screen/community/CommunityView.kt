package com.easylaw.app.ui.screen.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.viewmodel.CommunityViewModel

@Composable
fun CommunityView(
    modifier: Modifier,
    viewModel: CommunityViewModel,
    communityWrite: () -> Unit,
//    mainState: MainViewState
) {
    val viewState by viewModel.communityState.collectAsState()

//    LaunchedEffect(Unit) {
//        viewModel.loadCommunityPosts()
//    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFFF2F4F6)),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                Text(
                    text = "커뮤니티",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "나와 비슷한 상황의 사람들과 이야기를 나눠보세요.",
                    fontSize = 15.sp,
                    color = Color(0xFF4E5968),
                )
            }
        }
        FloatingActionButton(
            onClick = communityWrite,
            containerColor = Color(0xFF3182F6),
            contentColor = Color.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "글쓰기")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityFilterChips() {
    val filters = listOf("전체", "임금체불", "비자/체류", "생활법률", "부당해고")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters) { filter ->
            FilterChip(
                selected = (filter == "전체"),
                onClick = { },
                label = { Text(filter) },
                shape = RoundedCornerShape(20.dp),
            )
        }
    }
}

@Composable
fun CommunityPostItem(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = post.tag,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = Color(0xFF1976D2),
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "댓글: ${post.commentCount}", fontSize = 12.sp, color = Color.LightGray)
            }

            Text(
                text = post.title,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = post.content,
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))
        }
    }
}
