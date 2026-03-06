package com.easylaw.app.ui.screen.diagnosis

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easylaw.app.domain.model.Diagnosis
import com.easylaw.app.domain.model.DiagnosisPhase
import com.easylaw.app.domain.model.RetryActionType
import com.easylaw.app.viewModel.DiagnosisUiState
import com.easylaw.app.viewModel.DiagnosisViewModel
import kotlinx.coroutines.delay

@Composable
fun DiagnosisScreen(viewModel: DiagnosisViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
//    var currentAnswerText by remember { mutableStateOf("") }

    LaunchedEffect(uiState.messages.size) {
        val lastMessage = uiState.messages.lastOrNull()
        if (lastMessage != null && lastMessage !is Diagnosis.Loading) {
            delay(100)
            listState.animateScrollToItem(
                index = uiState.messages.lastIndex,
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
//                    currentAnswerText = currentAnswerText,
//                    onAnswerChange = { currentAnswerText = it },
//                    onAnswerSend = {
//                        viewModel.handleUserAnswerToQuestions(currentAnswerText)
//                        currentAnswerText = ""
//                    },
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
    val sttController =
        rememberSpeechRecognizerHandler(
            onFinalResult = { recognizedText ->
                val newText = if (userScenario.isBlank()) recognizedText else "$userScenario $recognizedText"
                onUserScenarioChange(newText.trim())
            },
        )

    Box(modifier = Modifier.fillMaxSize()) {
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
                color = Color.Black,
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
                trailingIcon = {
                    IconButton(onClick = sttController.toggleListening) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "음성 입력",
                            tint = Color.Gray,
                        )
                    }
                },
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartDiagnosis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                enabled = userScenario.isNotBlank() && !sttController.isSheetVisible,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("자가진단 시작하기", style = MaterialTheme.typography.labelLarge)
            }
        }
        SttBottomSheet(controller = sttController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisResultContent(
    listState: LazyListState,
    uiState: DiagnosisUiState,
//    currentAnswerText: String,
//    onAnswerChange: (String) -> Unit,
//    onAnswerSend: () -> Unit,
    onOptionSelected: (String) -> Unit,
    onRetry: (RetryActionType) -> Unit,
) {
//    val sttController = rememberSpeechRecognizerHandler(
//        onFinalResult = { recognizedText ->
//            val newText = if (currentAnswerText.isBlank()) recognizedText else "$currentAnswerText $recognizedText"
//            onAnswerChange(newText.trim())
//        }
//    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp),
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

//            if (uiState.currentPhase == DiagnosisPhase.AWAITING_ANSWERS) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .background(Color.White),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    TextField(
//                        value = currentAnswerText,
//                        onValueChange = onAnswerChange,
//                        modifier = Modifier.weight(1f),
//                        placeholder = { Text("직접 입력 또는 위의 버튼 선택...") },
//                        shape = RoundedCornerShape(12.dp),
//                        trailingIcon = {
//                            IconButton(onClick = sttController.toggleListening) {
//                                Icon(
//                                    imageVector = Icons.Default.Mic,
//                                    contentDescription = "음성 입력",
//                                    tint = Color.Gray
//                                )
//                            }
//                        }
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Button(
//                        onClick = onAnswerSend,
//                        enabled = currentAnswerText.isNotBlank() && !sttController.isSheetVisible
//                    ) {
//                        Text("전송")
//                    }
//                }
//            }
        }
//        SttBottomSheet(controller = sttController)
    }
}

enum class SttPhase(
    val message: String,
) {
    IDLE(""),
    CONNECTING("연결 중입니다..."),
    LISTENING("듣고 있어요... 말씀하세요."),
    PROCESSING("텍스트로 변환하는 중입니다..."),
}

data class SttController(
    val isSheetVisible: Boolean,
    val phase: SttPhase,
    val partialText: String,
    val toggleListening: () -> Unit,
    val stopListening: () -> Unit,
    val cancelListening: () -> Unit,
)

@Composable
fun rememberSpeechRecognizerHandler(onFinalResult: (String) -> Unit): SttController {
    val context = LocalContext.current
    val activity = context as? Activity

    val isPreview = LocalInspectionMode.current

    if (isPreview) {
        return SttController(
            isSheetVisible = false,
            phase = SttPhase.IDLE,
            partialText = "",
            toggleListening = {},
            stopListening = {},
            cancelListening = {},
        )
    }

    var isSheetVisible by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(SttPhase.IDLE) }
    var partialText by remember { mutableStateOf("") }

    val onFinalResultState = rememberUpdatedState(onFinalResult)

    val speechRecognizer =
        remember {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                null
            }
        }

    val closeAndReset = {
        isSheetVisible = false
        phase = SttPhase.IDLE
        partialText = ""
    }

    val recognitionListener =
        remember {
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    phase = SttPhase.LISTENING // 듣기 시작
                }

                override fun onBeginningOfSpeech() {}

                override fun onRmsChanged(rmsdB: Float) {}

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    phase = SttPhase.PROCESSING // 말하기 종료 후 변환 중
                }

                override fun onError(error: Int) {
                    closeAndReset()

                    when (error) {
                        SpeechRecognizer.ERROR_AUDIO ->
                            Toast.makeText(context, "오디오 녹음 오류입니다. 마이크 상태를 확인해 주세요.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_CLIENT ->
                            Toast.makeText(context, "클라이언트 측 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                            Toast.makeText(context, "마이크 권한이 없습니다. 설정에서 허용해 주세요.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_NETWORK ->
                            Toast.makeText(context, "네트워크 연결이 원활하지 않습니다.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                            Toast.makeText(context, "네트워크 연결 시간이 초과되었습니다.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_NO_MATCH ->
                            Toast.makeText(context, "인식된 결과가 없습니다. 다시 말씀해 주세요.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                            Toast.makeText(context, "서비스가 바쁩니다. 잠시 후 다시 시도해 주세요.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_SERVER ->
                            Toast.makeText(context, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()

                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                            Toast.makeText(context, "말씀이 없어 인식을 종료합니다.", Toast.LENGTH_SHORT).show()

                        else ->
                            Toast.makeText(context, "알 수 없는 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val partialText = matches[0]
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onFinalResultState.value(matches[0])
                    }
                    closeAndReset()
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?,
                ) {}
            }
        }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(recognitionListener)
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val startListeningInternal: () -> Unit = {
        isSheetVisible = true
        phase = SttPhase.CONNECTING
        partialText = ""

        val intent =
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ko-KR")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
        speechRecognizer?.startListening(intent)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                startListeningInternal()
            } else {
                Toast.makeText(context, "음성 인식을 위해 마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    val openSettings: () -> Unit = {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        context.startActivity(intent)
    }

    val toggleListening = {
        if (speechRecognizer == null) {
            Toast.makeText(context, "이 기기에서는 음성 인식을 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
        } else {
            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                startListeningInternal()
            } else {
                if (activity != null &&
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        Manifest.permission.RECORD_AUDIO,
                    )
                ) {
                    Toast.makeText(context, "법률 진단을 위해 마이크 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    Toast.makeText(context, "마이크 권한이 거부되어 있습니다. 설정에서 허용해주세요.", Toast.LENGTH_LONG).show()
//                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    openSettings()
                }
            }
        }
    }

    // 수동으로 입력을 완료하고 변환을 시작하는 함수
    val stopListening = {
        speechRecognizer?.stopListening()
        phase = SttPhase.PROCESSING
    }

    // 바텀 시트를 끌어내리거나 취소할 때 호출되는 함수
    val cancelListening = {
        speechRecognizer?.cancel()
        closeAndReset()
    }

    return SttController(
        isSheetVisible = isSheetVisible,
        phase = phase,
        partialText = partialText,
        toggleListening = toggleListening,
        stopListening = stopListening,
        cancelListening = cancelListening,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SttBottomSheet(controller: SttController) {
    if (controller.isSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            // 사용자가 화면 밖을 터치하거나 시트를 밑으로 스크롤해서 내리면 STT를 취소합니다.
            onDismissRequest = { controller.cancelListening() },
            sheetState = sheetState,
            containerColor = Color.White,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 24.dp, end = 24.dp, top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // 마이크 아이콘 (듣는 중일 때 붉은색 활성화)
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "마이크",
                    tint = if (controller.phase == SttPhase.LISTENING) Color.Red else Color.LightGray,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 상태 표시 (연결 중..., 듣고 있어요..., 변환 중...)
                Text(
                    text = controller.phase.message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 실시간 부분 인식 결과 노출 영역 (회색 글씨)
                Text(
                    text = if (controller.partialText.isBlank()) "..." else controller.partialText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (controller.partialText.isBlank()) Color.LightGray else Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 입력 완료 버튼
                Button(
                    onClick = { controller.stopListening() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = controller.phase == SttPhase.LISTENING || controller.phase == SttPhase.CONNECTING,
                ) {
                    Text("입력 완료")
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
        BotBubble(text = text)
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
fun parseMarkdownToAnnotatedString(text: String): AnnotatedString {
    val primaryColor = MaterialTheme.colorScheme.primary
    return buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) { // ** 사이에 있는 텍스트 (홀수 인덱스)
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = primaryColor)) {
                    append(part)
                }
            } else { // 일반 텍스트
                append(part)
            }
        }
    }
}

@Composable
fun BotBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = Color(0xFFE0E0E0),
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            shadowElevation = 2.dp,
        ) {
            Text(
                text = parseMarkdownToAnnotatedString(text),
                color = Color(0xFF333333),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f),
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

// @androidx.compose.ui.tooling.preview.Preview(showBackground = true)
// @Composable
// fun DiagnosisScreenPreview() {
//    // 실제 ViewModel 대신 더미 데이터를 넣어 테마를 확인합니다.
//    MaterialTheme {
//        DiagnosisFormContent(userScenario = "", onUserScenarioChange = {}, onStartDiagnosis = {})
//    }
// }

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun DiagnosisResultContentPreview() {
    val dummyMessages =
        listOf(
            Diagnosis.Bot("안녕하세요! 어떤 법률적 도움이 필요하신가요?"),
            Diagnosis.User("임금 체불 때문에 문의 드립니다."),
            Diagnosis.BotWithOptions(
                text = "미지급된 임금이 총 얼마 정도인가요?",
                options = listOf("100만원 미만", "100~500만원", "500만원 이상"),
            ),
            Diagnosis.ErrorRetry("에러 발생", RetryActionType.FOLLOW_UP_QUESTIONS),
        )

    val dummyUiState =
        DiagnosisUiState(
            messages = dummyMessages,
            currentPhase = DiagnosisPhase.AWAITING_ANSWERS,
            isShowingResults = true,
        )

    MaterialTheme {
        Surface(color = Color.White) {
            DiagnosisResultContent(
                listState = rememberLazyListState(),
                uiState = dummyUiState,
//                currentAnswerText = "직접 입력 중인 텍스트",
//                onAnswerChange = {},
//                onAnswerSend = {},
                onOptionSelected = {},
                onRetry = {},
            )
        }
    }
}
