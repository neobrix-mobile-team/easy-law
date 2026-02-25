package com.easylaw.app.ui.screen.lawConsult

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.easylaw.app.dto.precModel
import com.easylaw.app.ui.components.CommonButton
import com.easylaw.app.ui.components.CommonTextField

@Composable
fun LawConsultView(
    modifier: Modifier,
    viewModel: LawConsultViewModel,
) {
    val lawState by viewModel.lawConsultViewState.collectAsState()
    val isRequire = lawState.situationInput.isEmpty()

    val scrollState = rememberScrollState()

    Box {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(24.dp),
        ) {
            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                Text(
                    text = "상황 진단하기",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                )
                Text(
                    text = "정확한 분석을 위해 아래 항목을 채워주세요.",
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
                    value = lawState.situationInput,
                    onValueChange = { viewModel.onChangedTextField(lawState.copy(situationInput = it)) },
                    placeholder = "예: 임금 체불, 비자 문제 등",
                    isRequire = true, // 별표 표시용
                    isError = isRequire,
                    errorText = if (!isRequire) "" else "필수 항목입니다.",
                )

                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    title = "법원종류",
                    value = lawState.lawTypeInput,
                    onValueChange = { viewModel.onChangedTextField(lawState.copy(lawTypeInput = it)) },
                    placeholder = "예: 대법원, 서울고등법원 등 (선택)",
                )

                CommonTextField(
                    modifier = Modifier.fillMaxWidth(),
                    title = "상세 내용",
                    value = lawState.detailInput,
                    onValueChange = { viewModel.onChangedTextField(lawState.copy(detailInput = it)) },
                    placeholder = "발생한 일을 구체적으로 적어주세요 (선택)",
                    singleLine = false, // 상세 내용은 여러 줄 입력 가능하게
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            CommonButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .height(60.dp),
                onClick = {
                    viewModel.searchPrecedents()
                },
                color = Color(0xFF64B5F6),
                icon = Icons.Default.AutoAwesome,
                text = "판례 검색",
                isEnable = !isRequire,
            )
        }

        if (lawState.precSuccess && lawState.precList.isNotEmpty()) {
            LawSearchResultDialog(
                precList = lawState.precList,
                onDismiss = {
                    viewModel.onDismiss()
                },
            )
        }
    }
}

@Composable
fun LawSearchResultDialog(
    precList: List<precModel>,
    onDismiss: () -> Unit,
) {
    Dialog(
        // 창의 바깥 터치 제어
        onDismissRequest = {},
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "판례 검색 결과",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = "총 ${precList.size}건이 검색되었습니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Spacer(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color = Color(0xFFF1F3F5)),
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 검색 결과 리스트
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(precList) { prec ->
                        PrecedentItemRow(prec)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 하단 버튼
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                ) {
                    Text("확인", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PrecedentItemRow(item: precModel) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable(
                    onClick = {
                        Log.d("판례일렬번호", item.serialNumber)
                    },
                ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color(0xFFE9ECEF)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // 1. 사건명
            Text(
                text = item.caseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212529),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. 부가 정보 (사건종류, 법원명, 판결유형 )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = item.caseTypeName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1976D2),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.courtName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6C757D),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.judgment,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6C757D),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 3. 선고일자
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFADB5BD),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "선고일자: ${item.judgmentDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6C757D),
                )
            }
        }
    }
}
