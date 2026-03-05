package com.easylaw.app.ui.screen.diagnosis

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylaw.app.domain.model.Diagnosis
import com.easylaw.app.domain.model.DiagnosisPhase
import com.easylaw.app.domain.model.RetryActionType
import com.easylaw.app.viewModel.DiagnosisUiState
import com.easylaw.app.viewModel.DiagnosisViewModel

@Composable
fun DiagnosisScreen(viewModel: DiagnosisViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var currentAnswerText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(
                index = uiState.messages.lastIndex,
                scrollOffset = 0, // 상단 정렬
            )
        }
    }

    Scaffold(
        topBar = { EasyLawTopBar() },
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            if (!uiState.isShowingResults) {
                DiagnosisFormContent(
                    userScenario = viewModel.userScenarioInput,
                    onUserScenarioChange = viewModel::onUserScenarioInputChange,
                    onStartDiagnosis = viewModel::onStartDiagnosis,
                )
            } else {
                DiagnosisResultContent(
                    listState = listState,
                    uiState = uiState,
                    currentAnswerText = currentAnswerText,
                    onAnswerChange = { currentAnswerText = it },
                    onAnswerSend = {
                        viewModel.handleUserAnswerToQuestions(currentAnswerText)
                        currentAnswerText = ""
                    },
                    onOptionSelected = { selectedOption ->
                        viewModel.handleUserAnswerToQuestions(selectedOption)
                    },
                    onRetry = { retryType ->
                        viewModel.retryAction(retryType)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisFormContent(
    userScenario: String,
    onUserScenarioChange: (String) -> Unit,
    onStartDiagnosis: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
    ) {
        Text(
            text = "자가진단",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "문제사항을 최대한 자세히 적어주시면,\n정확한 법률 진단을 받을 수 있습니다.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userScenario,
            onValueChange = onUserScenarioChange,
            modifier =
                Modifier
                    .fillMaxWidth(),
            label = { Text("법률 문제 상황 설명") },
            placeholder = {
                Text(
                    text = "사장이 3개월동안 월급을 안줬는데...",
                    color = Color.Gray.copy(alpha = 0.5f),
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1F5B9C),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    errorBorderColor = Color.Red,
                ),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartDiagnosis,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            enabled = userScenario.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("자가진단 시작하기", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisResultContent(
    listState: LazyListState,
    uiState: DiagnosisUiState,
    currentAnswerText: String,
    onAnswerChange: (String) -> Unit,
    onAnswerSend: () -> Unit,
    onOptionSelected: (String) -> Unit,
    onRetry: (RetryActionType) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.messages) { message ->
                when (message) {
                    is Diagnosis.User -> UserBubble(message.text)
                    is Diagnosis.Bot -> BotBubble(message.text)
                    is Diagnosis.BotWithOptions ->
                        BotWithOptionsBubble(
                            text = message.text,
                            options = message.options,
                            onOptionSelected = onOptionSelected,
                            isEnabled = uiState.currentPhase == DiagnosisPhase.AWAITING_ANSWERS && message == uiState.messages.last(),
                        )

                    is Diagnosis.ErrorRetry ->
                        ErrorRetryBubble(
                            text = message.text,
                            onRetry = { onRetry(message.retryActionType) },
                        )

                    is Diagnosis.Loading -> LoadingBubble()
                }
            }
        }

        if (uiState.currentPhase == DiagnosisPhase.AWAITING_ANSWERS) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color.White),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = currentAnswerText,
                    onValueChange = onAnswerChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("직접 입력 또는 위의 버튼 선택...") },
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAnswerSend,
                    enabled = currentAnswerText.isNotBlank(),
                ) {
                    Text("전송")
                }
            }
        }
    }
}

@Composable
fun BotWithOptionsBubble(
    text: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    isEnabled: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
    ) {
        BotBubble(text = text) // 질문 텍스트는 기존 BotBubble 재사용

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                OutlinedButton(
                    onClick = { onOptionSelected(option) },
                    enabled = isEnabled,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = option)
                }
            }
        }
    }
}

// 처음 사용된 구문 설명: 통신 에러 상황에서 사용자에게 시각적 경고(Error Color)를 주고 버튼을 통해 재요청을 유도하는 디자인입니다.
@Composable
fun ErrorRetryBubble(
    text: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
            Text("분석 요청")
        }
    }
}

@Composable
fun EasyLawTopBar() {
}

@Composable
fun UserBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun BotBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = Color(0xFFE0E0E0),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
        ) {
            Text(
                text = text,
                color = Color.Black,
                // [수정] modifier와 style 사이 쉼표(,) 누락 해결
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "분석 중...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
