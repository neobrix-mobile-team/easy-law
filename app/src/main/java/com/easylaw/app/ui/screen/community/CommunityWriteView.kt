package com.easylaw.app.ui.screen.community

import CommonImageButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.easylaw.app.ui.components.CommonButton
import com.easylaw.app.ui.components.CommonDialog
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

    /*
        갤러리 실행기
        1. launch : 클릭 시 앱은 잠시 멈추고 시스템 갤러리가 화면을 덮는다.
        2. 사용자가 한 장 고르면 해당 주소를 가지고 앱으로 복귀
        3. 콜백 실행
        4. 저장

     */
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri ->
            uri?.let {
                viewModel.onImageAdded(it.toString())
            }
        }
    if (viewState.isShowDialog) {
        CommonDialog(
            title = "사진 추가 제한",
            desc = "사진은 최대 3장까지만 등록할 수 있습니다.",
            icon = Icons.Default.Close,
            onConfirm = { viewModel.closeShowDialog() },
        )
    }
//    viewState.previewImage?.let { uri ->
//        Dialog(
//            onDismissRequest = { viewModel.onImagePreviewDismissed() },
//            properties = DialogProperties(usePlatformDefaultWidth = true)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(500.dp)
//                    .clip(RoundedCornerShape(24.dp))
//                    .background(Color.White)
//            ) {
//                AndroidView(
//                    factory = { context ->
//                        PhotoView(context).apply {
//                            load(uri)
//                        }
//                    },
//                    modifier = Modifier.fillMaxSize().padding(12.dp)
//                )
//
//                IconButton(
//                    onClick = { viewModel.onImagePreviewDismissed() },
//                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = "닫기",
//                        tint = Color(0xFF8B95A1)
//                    )
//                }
//            }
//        }
//    }
    Scaffold(
        bottomBar = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .navigationBarsPadding() // 하단바 공간 확보
                        .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                CommonButton(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    text = "작성 완료",
                    isEnable = false,
                    onClick = { },
                    color = Color(0xFF3182F6),
                    icon = Icons.Default.Check,
                )
            }
        },
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // 하단 버튼 높이만큼 자동 패딩
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 분야 선택 섹션
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

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                containerColor = Color(0xFFF2F4F6),
                                labelColor = Color(0xFF4E5968),
                                selectedContainerColor = Color(0xFF3182F6).copy(alpha = 0.1f),
                                selectedLabelColor = Color(0xFF3182F6),
                            ),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                borderColor = Color.Transparent,
                                selected = isSelected,
                                selectedBorderColor = Color(0xFF3182F6),
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
                        text = "첨부된 사진 ${viewState.selectedImages.size}/3",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B95A1),
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        item {
                            CommonImageButton(
                                onClick = {
                                    if (viewState.selectedImages.size < 3) {
                                        galleryLauncher.launch("image/*")
                                    } else {
                                        viewModel.onShowDialog()
                                    }
                                },
                            )
                        }

                        items(viewState.selectedImages) { imageUri ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(56.dp)
                                        .clickable { viewModel.onImagePreview(imageUri) }
                                        .background(Color.White, RoundedCornerShape(12.dp)),
                            ) {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop,
                                )

                                Box(
                                    modifier =
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 4.dp, y = (-4).dp)
                                            .size(18.dp)
                                            .background(Color(0xFF4E5968), CircleShape)
                                            .clickable { viewModel.removeSelectedImage(imageUri) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "삭제",
                                        tint = Color.White,
                                        modifier = Modifier.size(10.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
