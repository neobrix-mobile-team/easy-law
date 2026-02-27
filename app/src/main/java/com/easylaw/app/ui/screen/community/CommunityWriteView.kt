package com.easylaw.app.ui.screen.community

import CommonImageButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.ui.components.CommonTextField
import com.easylaw.app.viewmodel.CommunityWriteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityWriteView(
    modifier: Modifier = Modifier,
    viewModel: CommunityWriteViewModel,
) {
    val viewState by viewModel.commnuityWriteViewState.collectAsState()
    val scrollState = rememberScrollState()

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let { }
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .background(Color.White),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        ) {
            Spacer(
                modifier = Modifier.height(24.dp),
            )
            // 헤더 텍스트
            Text(
                text = "어떤 분야의 고민인가요?",
                style =
                    TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF4E5968),
                        fontWeight = FontWeight.Medium,
                    ),
                modifier = Modifier.padding(vertical = 12.dp),
            )

            // 2. 카테고리 칩 리스트
            LazyRow(
//                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(viewState.categories) { category ->
                    val isSelected = (viewState.selectedCategory == category)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = {
                            Text(
                                text = category,
                                style =
                                    TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    ),
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFFF2F4F6), // 미선택 배경
                                labelColor = Color(0xFF4E5968), // 미선택 글자
                                selectedContainerColor = Color(0xFF3182F6).copy(alpha = 0.1f), // 선택 배경 (연한 블루)
                                selectedLabelColor = Color(0xFF3182F6), // 선택 글자 (진한 블루)
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                borderColor = Color.Transparent, // 미선택 테두리 없음
                                selected = isSelected,
                                selectedBorderColor = Color(0xFF3182F6), // 선택 테두리 블루
                                borderWidth = 2.dp,
                                selectedBorderWidth = 2.dp,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            CommonTextField(
                title = "제목",
                value = viewState.communityWriteTitleField,
                placeholder = "제목을 입력해주세요.",
                onValueChange = { viewModel.onTitleFieldChanged(it) },
            )
            CommonTextField(
                title = "본문",
                value = viewState.communityWriteContentField,
                placeholder = "본문을 입력해주세요.",
                singleLine = false,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                onValueChange = { viewModel.onContentFieldChanged(it) },
            )

            Spacer(modifier = Modifier.height(24.dp))
            CommonImageButton(
                onClick = { galleryLauncher.launch("image/*") },
            )
//                    Card(
//                        onClick = { galleryLauncher.launch("image/*") },
//                        modifier = Modifier.size(80.dp),
//                        shape = RoundedCornerShape(12.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F4F6))
//                    ) {
//                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//                            Icon(
//                                imageVector = Icons.Default.AddPhotoAlternate, // 갤러리 아이콘
//                                contentDescription = "사진 추가",
//                                tint = Color(0xFF8B95A1)
//                            )
//                        }
//                    }

//                items(viewState.selectedImages) { imageUri ->
//                    Box(modifier = Modifier.size(80.dp)) {
//                        // Coil의 AsyncImage를 사용하여 이미지 렌더링
//                        AsyncImage(
//                            model = imageUri,
//                            contentDescription = null,
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .clip(RoundedCornerShape(12.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//
//                        IconButton(
//                            onClick = { viewModel.onImageRemoved(imageUri) },
//                            modifier = Modifier
//                                .align(Alignment.TopEnd)
//                                .padding(4.dp)
//                                .size(20.dp)
//                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Close,
//                                contentDescription = "삭제",
//                                tint = Color.White,
//                                modifier = Modifier.size(12.dp)
//                            )
//                        }
//                    }
//                }
        }
    }
}
