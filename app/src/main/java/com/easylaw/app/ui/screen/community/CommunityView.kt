package com.easylaw.app.ui.screen.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
            Icon(Icons.Default.Add, contentDescription = "글쓰기", modifier = Modifier.size(28.dp))
        }
    }
}
