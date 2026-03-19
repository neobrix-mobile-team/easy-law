package com.easylaw.app.ui.screen

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylaw.app.R
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.util.debouncedClickable
import com.easylaw.app.viewModel.CourtTypeOption
import com.easylaw.app.viewModel.DetailViewMode
import com.easylaw.app.viewModel.LegalSearchUiState
import com.easylaw.app.viewModel.LegalSearchViewModel

@Composable
fun LegalSearchRoute(viewModel: LegalSearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayResults by viewModel.displayResults.collectAsStateWithLifecycle()
    val filterKeyword by viewModel.filterKeyword.collectAsStateWithLifecycle()

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
//                pagingItems = searchResults,
                precedents = displayResults,
                filterKeyword = filterKeyword,
                onFilterKeywordChange = viewModel::updateListFilterText,
                onPrecedentClick = viewModel::onPrecedentClick,
                onDismiss = viewModel::closeResults,
                onVisibleItemsChanged = viewModel::translateVisibleItems,
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
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(text = stringResource(R.string.legal_search_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.legal_search_subtitle), fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.legal_search_situation_label), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "*", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = uiState.situation,
                onValueChange = onSituationChange,
                placeholder = stringResource(R.string.legal_search_situation_placeholder),
                isError = uiState.isSituationError,
            )

            if (uiState.isSituationError) {
                Text(
                    text = stringResource(R.string.required_field),
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = stringResource(R.string.legal_search_court_label), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            CourtTypeSpinner(
                selectedOption = uiState.selectedCourt,
                onOptionSelected = onCourtTypeChange,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = stringResource(R.string.legal_search_detail_label), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField(
                value = uiState.details,
                onValueChange = onDetailsChange,
                placeholder = stringResource(R.string.legal_search_detail_placeholder),
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
                    Text(text = stringResource(R.string.legal_search_analyzing), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        text = stringResource(R.string.legal_search_btn),
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
            value = stringResource(selectedOption.displayName),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
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
                    text = { Text(text = stringResource(option.displayName), color = Color.Black) },
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
//    pagingItems: LazyPagingItems<Precedent>,
    precedents: List<Precedent>,
    filterKeyword: String,
    onFilterKeywordChange: (String) -> Unit,
    onPrecedentClick: (Precedent) -> Unit,
    onDismiss: () -> Unit,
    onVisibleItemsChanged: (List<Precedent>) -> Unit,
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
                        Text(text = stringResource(R.string.legal_search_result_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Spacer(modifier = Modifier.height(4.dp))

                        // 키워드 노출
                        Text(
                            text = "${stringResource(R.string.legal_search_ai_analyzing)}: ${uiState.extractedKeyword}",
                            fontSize = 14.sp,
                            color = Color(0xFF1967D2),
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (precedents.isEmpty() && uiState.totalSearchCount == 0) {
                            Text(text = stringResource(R.string.legal_search_no_result), fontSize = 14.sp, color = Color.Gray)
                        } else {
                            val resultText =
                                if (filterKeyword.isNotBlank()) {
                                    stringResource(R.string.legal_search_result_filtered, precedents.size)
                                } else {
                                    stringResource(R.string.legal_search_result_total, uiState.totalSearchCount)
                                }
                            Text(text = resultText, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.legal_search_close_desc),
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.listFilterText,
                    onValueChange = onFilterKeywordChange,
                    placeholder = { Text(stringResource(R.string.legal_search_re_search_placeholder)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.legal_search_re_search_desc),
                        )
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1967D2),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                        ),
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))

                // 검색 결과 리스트
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // paging
//                    items(count = pagingItems.itemCount) { index ->
//                        val precedent = pagingItems[index]
//                        if (precedent != null) {
//                            PrecedentCard(precedent) { onPrecedentClick(precedent) }
//                        }
//                    }

                        Log.d("Translation_LOG", "LazyColumn items 렌더링 - precedents.size: ${precedents.size}")
                        items(precedents, key = { it.id }) { precedent ->

                            LaunchedEffect(precedent.id) {
                                onVisibleItemsChanged(listOf(precedent))
                            }

                            PrecedentCard(
                                precedent = precedent,
                                translatedTitle = uiState.translatedTitles[precedent.title],
                                translatedCategory = uiState.translatedTitles[precedent.category],
                                translatedCourt = uiState.translatedTitles[precedent.court],
                                translatedJudgmentType = uiState.translatedTitles[precedent.judgmentType],
                                onClick = { onPrecedentClick(precedent) },
                            )
                        }

//                    if (pagingItems.loadState.append is LoadState.Loading) {
//                        item {
//                            Box(
//                                modifier =
//                                    Modifier
//                                        .fillMaxWidth()
//                                        .padding(16.dp),
//                                contentAlignment = Alignment.Center,
//                            ) {
//                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF6B9DE8))
//                            }
//                        }
//                    }

                        if (precedents.isEmpty() && uiState.listFilterText.isNotBlank()) {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(text = stringResource(R.string.legal_search_filter_no_result), color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6B9DE8))
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
                        Text(text = stringResource(R.string.confirm), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PrecedentCard(
    precedent: Precedent,
    translatedTitle: String? = null,
    translatedCategory: String? = null,
    translatedCourt: String? = null,
    translatedJudgmentType: String? = null,
    onClick: () -> Unit,
) {
    // 번역값이 있으면 번역값 사용, 없으면 원문 표시
    val displayTitle = translatedTitle ?: precedent.title
    val displayCategory = translatedCategory ?: precedent.category
    val displayCourt = translatedCourt ?: precedent.court
    val displayJudgmentType = translatedJudgmentType ?: precedent.judgmentType

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
            text = displayTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (displayCategory.isNotEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .background(Color(0xFFE8F0FE), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(text = displayCategory, color = Color(0xFF1967D2), fontSize = 12.sp)
                }
            }

            if (displayCourt.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = displayCourt, color = Color.Gray, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (displayJudgmentType.isNotEmpty()) {
                    Text(text = "⚖️", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = displayJudgmentType,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                text = stringResource(R.string.legal_search_verdict_date, precedent.date ?: ""),
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
            )
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
                contentDescription = if (isExpanded) stringResource(R.string.legal_search_collapse_desc) else stringResource(R.string.legal_search_expand_desc),
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
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.legal_search_close_desc))
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
                        text = uiState.detailTitle.ifBlank { detail.title },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val tabs = listOf(DetailViewMode.ORIGINAL to stringResource(R.string.legal_search_tab_original), DetailViewMode.SUMMARY to stringResource(R.string.legal_search_tab_ai_summary))
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
                                        ExpandableLegalSection(title = stringResource(R.string.legal_search_section_issue), content = detail.issue)
                                    }
                                    if (detail.summary.isNotBlank()) {
                                        ExpandableLegalSection(title = stringResource(R.string.legal_search_section_summary), content = detail.summary)
                                    }
                                    if (detail.content.isNotBlank()) {
                                        ExpandableLegalSection(title = stringResource(R.string.legal_search_section_content), content = detail.content)
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
                                            stringResource(R.string.legal_search_summarizing),
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
                        val uriHandler = LocalUriHandler.current
                        val link = uiState.selectedPrecedentLink
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
