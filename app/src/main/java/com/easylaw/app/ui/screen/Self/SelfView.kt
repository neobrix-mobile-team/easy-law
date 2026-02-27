package com.easylaw.app.ui.screen.Self

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.easylaw.app.ui.components.CommonButton
import com.easylaw.app.ui.components.CommonTextField
import com.easylaw.app.viewmodel.SelfViewModel

@Composable
fun SelfView(
    modifier: Modifier,
    viewModel: SelfViewModel,
) {
    val viewState by viewModel.selfViewState.collectAsState()
    val isRequire = viewState.situationInput.isEmpty()

    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                Text(
                    text = "자가진단",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                )
                Text(
                    text = "몇 가지만 체크하면, 나에게 딱 맞는 쉬운 해결 가이드를 바로 보여드려요.",
                    fontSize = 14.sp,
                    color = Color(0xFF74777F),
                )
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    title = "어떤 상황인가요?",
                    value = viewState.situationInput,
                    onValueChange = {
                        viewModel.onChangedTextField(viewState.copy(situationInput = it))
                    },
                    placeholder = "구체적인 상황을 알려주세요",
                    isRequire = true, // 별표 표시용
                    isError = isRequire,
                    errorText = if (!isRequire) "" else "필수 항목입니다.",
                )

                if (viewState.selfSuccess && viewState.selfList.size >= 3) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = Color(0xFFF0F4F8), // 부드러운 배경색
                            ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🔍", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = viewState.selfList[0],
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C1E),
                                )
                            }

                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                            Column {
                                Text(
                                    text = "AI의 쉬운 설명",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6200EE), // 강조색
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewState.selfList[1],
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = Color(0xFF44474E),
                                )
                            }

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                            ) {
                                Row {
                                    Text(text = "✅", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "준비해야 할 것: ${viewState.selfList[2]}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1A1C1E),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            CommonButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(60.dp),
                onClick = {
                    viewModel.searchSelf()
                },
                color = Color(0xFF64B5F6),
                icon = Icons.Default.AutoAwesome,
                text = "진단하기",
                isEnable = !isRequire,
            )
        }
    }
}
