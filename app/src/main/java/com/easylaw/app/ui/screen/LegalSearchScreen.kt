package com.easylaw.app.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.util.debouncedClickable
import com.easylaw.app.viewmodel.CourtTypeOption
import com.easylaw.app.viewmodel.DetailViewMode
import com.easylaw.app.viewmodel.LegalSearchUiState
import com.easylaw.app.viewmodel.LegalSearchViewModel

@Composable
fun LegalSearchRoute(viewModel: LegalSearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        SituationDiagnosisScreen(
            uiState = uiState,
            onSituationChange = viewModel::updateSituation,
            onCourtTypeChange = viewModel::updateCourtType,
            onDetailsChange = viewModel::updateDetails,
            onSearchClick = viewModel::searchLegalAdvice,
        )

        if (uiState.showResults) {
            PrecedentResultDialog(
                uiState = uiState,
                pagingItems = searchResults,
                onPrecedentClick = viewModel::onPrecedentClick,
                onDismiss = viewModel::closeResults,
            )
        }

        if (uiState.showDetailDialog) {
            PrecedentDetailDialog(
                uiState = uiState,
                onDismiss = viewModel::closeDetailDialog,
                onTabSelected = viewModel::toggleDetailViewMode,
            )
        }
    }
}

@Composable
fun SituationDiagnosisScreen(
    uiState: LegalSearchUiState,
    onSituationChange: (String) -> Unit,
    onCourtTypeChange: (CourtTypeOption) -> Unit,
    onDetailsChange: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .imePadding()
                .safeDrawingPadding()
                .padding(24.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(text = "상황 진단하기", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "정확한 분석을 위해 아래 항목을 채워주세요.", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "어떤 상황인가요?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "*", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = uiState.situation,
                onValueChange = onSituationChange,
                placeholder = "예: 임금을 석달째 못받았어요",
                isError = uiState.isSituationError,
            )

            if (uiState.isSituationError) {
                Text(
                    text = "필수 항목입니다.",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "법원종류", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            CourtTypeSpinner(
                selectedOption = uiState.selectedCourt,
                onOptionSelected = onCourtTypeChange,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = "상세 내용", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = uiState.details,
                onValueChange = onDetailsChange,
                placeholder = "발생한 일을 구체적으로 적어주세요 (선택)",
                minLines = 3,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        val isButtonEnabled = uiState.situation.isNotBlank() && !uiState.isLoadingGemini

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (isButtonEnabled) Color.Black else Color(0xFFE0E0E0))
                    .debouncedClickable { if (isButtonEnabled) onSearchClick() },
            contentAlignment = Alignment.Center,
        ) {
            if (uiState.isLoadingGemini) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "키워드 분석 중...", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (isButtonEnabled) Color.White else Color.Gray,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "판례 검색",
                        color = if (isButtonEnabled) Color.White else Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourtTypeSpinner(
    selectedOption: CourtTypeOption,
    onOptionSelected: (CourtTypeOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedOption.displayName,
            onValueChange = {},
            readOnly = true, // 사용자가 직접 타이핑할 수 없게 막음
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            // 메뉴가 텍스트 필드 바로 아래에 뜨도록 고정하는 역할
            shape = RoundedCornerShape(16.dp),
            colors =
                ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFFE0E0E0),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White),
        ) {
            // Enum 클래스에 정의된 모든 옵션을 리스트로 뿌려줍니다.
            CourtTypeOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option.displayName, color = Color.Black) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    minLines: Int = 1,
) {
    val borderColor = if (isError) Color.Red else Color(0xFFE0E0E0)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = Color(0xFFBDBDBD), fontSize = 16.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle =
                androidx.compose.ui.text
                    .TextStyle(fontSize = 16.sp, color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            minLines = minLines,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecedentResultDialog(
    uiState: LegalSearchUiState,
    pagingItems: LazyPagingItems<Precedent>,
    onPrecedentClick: (Precedent) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 헤더
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {
                        Text(text = "판례 검색 결과", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))

                        // 키워드 노출
                        Text(
                            text = "AI 분석 키워드: ${uiState.extractedKeyword}",
                            fontSize = 14.sp,
                            color = Color(0xFF1967D2),
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (pagingItems.loadState.refresh is LoadState.Loading) {
                            Text(text = "법령 데이터를 불러오는 중입니다...", fontSize = 14.sp, color = Color.Gray)
                        } else {
                            Text(
                                text = "총 ${uiState.totalSearchCount}건이 검색되었습니다.",
                                fontSize = 14.sp,
                                color = Color.Gray,
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFEEEEEE))

                // 검색 결과 리스트
                LazyColumn(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(count = pagingItems.itemCount) { index ->
                        val precedent = pagingItems[index]
                        if (precedent != null) {
                            PrecedentCard(precedent) { onPrecedentClick(precedent) }
                        }
                    }

                    if (pagingItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF6B9DE8))
                            }
                        }
                    }
                }

                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B9DE8)),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(text = "확인", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PrecedentCard(
    precedent: Precedent,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                .background(Color.White, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
                .padding(20.dp),
    ) {
        Text(
            text = precedent.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (precedent.category.isNotEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .background(Color(0xFFE8F0FE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(text = precedent.category, color = Color(0xFF1967D2), fontSize = 12.sp)
                }
            }

            if (precedent.court.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = precedent.court, color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "선고일자: ${precedent.date}", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun ExpandableLegalSection(
    title: String,
    content: String,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFAFAFA))
                .clickable { isExpanded = !isExpanded } // 터치 시 확장/축소 토글
                .padding(16.dp)
                .animateContentSize(), // 부드러운 전개 애니메이션
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color(0xFF1967D2), fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "접기" else "펼치기",
                tint = Color.Gray,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = content,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = Color.DarkGray,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2, // 접혀있을 때는 최대 2줄 제한
            overflow = TextOverflow.Ellipsis, // 2줄 초과 시 "..." 표시
        )
    }
}

// 본문 팝업
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecedentDetailDialog(
    uiState: LegalSearchUiState,
    onDismiss: () -> Unit,
    onTabSelected: (DetailViewMode) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 닫기 버튼
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                if (uiState.isDetailLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF6B9DE8))
                    }
                } else if (uiState.currentPrecedentDetail != null) {
                    val detail = uiState.currentPrecedentDetail

                    // 2. 판례 제목
                    Text(
                        text = detail.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // 생소한 구문 설명: TabRow를 사용하여 '원문'과 '요약' 탭을 구성합니다.
                    // 선택된 탭에 따라 하단 콘텐츠가 동적으로 바뀝니다.
                    val tabs = listOf(DetailViewMode.ORIGINAL to "원문", DetailViewMode.SUMMARY to "AI 요약")
                    val selectedTabIndex = tabs.indexOfFirst { it.first == uiState.detailViewMode }

                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color(0xFF1967D2),
                            )
                        },
                    ) {
                        tabs.forEachIndexed { index, (mode, title) ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { onTabSelected(mode) },
                                text = { Text(title, fontWeight = FontWeight.Bold) },
                            )
                        }
                    }

                    // 3. 탭에 따른 본문 콘텐츠 출력 (스크롤 가능)
                    val scrollState = rememberScrollState()
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(24.dp)
                                .verticalScroll(scrollState),
                    ) {
                        when (uiState.detailViewMode) {
                            DetailViewMode.ORIGINAL -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    if (detail.issue.isNotBlank()) {
                                        ExpandableLegalSection(title = "【판시사항】", content = detail.issue)
                                    }
                                    if (detail.summary.isNotBlank()) {
                                        ExpandableLegalSection(title = "【판결요지】", content = detail.summary)
                                    }
                                    if (detail.content.isNotBlank()) {
                                        ExpandableLegalSection(title = "【판례내용(이유)】", content = detail.content)
                                    }
                                }
                            }

                            DetailViewMode.SUMMARY -> {
                                if (uiState.isSummaryLoading) {
                                    Column(
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(top = 40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF1967D2))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "판결문을 분석하여\n요약하고 있습니다...",
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            color = Color.Gray,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = uiState.summaryText,
                                        fontSize = 16.sp,
                                        lineHeight = 26.sp,
                                        color = Color.Black,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("판례 상세 정보를 불러올 수 없습니다.")
                        val uriHandler = LocalUriHandler.current
                        val link = uiState.selectedPrecedentLink
                        // 서버가 "/DRF/..." 처럼 절대 경로를 주지 않을 경우를 대비해 베이스 URL을 조립해줍니다.
                        val fullUrl = if (link.startsWith("/")) "https://www.law.go.kr$link" else link
                        if (fullUrl.isNotBlank()) {
                            uriHandler.openUri(fullUrl)
                            onDismiss()
                        }
                    }
                }
            }
        }
    }
}
