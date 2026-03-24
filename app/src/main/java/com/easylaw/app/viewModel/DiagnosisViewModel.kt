package com.easylaw.app.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.domain.model.Diagnosis
import com.easylaw.app.domain.model.DiagnosisPhase
import com.easylaw.app.domain.model.RetryActionType
import com.easylaw.app.domain.usecase.GenerateDiagnosisGuideUseCase
import com.easylaw.app.domain.usecase.GetFollowUpQuestionUseCase
import com.easylaw.app.util.PreferenceManager
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosisUiState(
    val messages: List<Diagnosis> = emptyList(),
    val currentPhase: DiagnosisPhase = DiagnosisPhase.IDLE,
    val isShowingResults: Boolean = false,
    val questionCount: Int = 0,
    val streamingText: String = "",
)

@HiltViewModel
class DiagnosisViewModel
    @Inject
    constructor(
        private val getFollowUpQuestionUseCase: GetFollowUpQuestionUseCase,
        private val generateDiagnosisGuideUseCase: GenerateDiagnosisGuideUseCase,
        private val preferenceManager: PreferenceManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DiagnosisUiState())
        val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

        private val conversationHistory = mutableListOf<Content>()

        private var capturedLanguage: String = "ko"

        var userScenarioInput by mutableStateOf("")
            private set

        init {
            preferenceManager.languageState
                .drop(1)
                .onEach { newLanguage ->
                    Log.d("Diagnosis_LOG", "[VM] 언어 변경 감지: $newLanguage → 히스토리 초기화")
                    resetDiagnosis()
                }.launchIn(viewModelScope)
        }

        fun onUserScenarioInputChange(newValue: String) {
            userScenarioInput = newValue
        }

        fun onStartDiagnosis() {
            if (userScenarioInput.isBlank()) return

            capturedLanguage = preferenceManager.languageState.value
            Log.d("Diagnosis_LOG", "[VM] 진단 시작 - 언어 캡처: $capturedLanguage")

            conversationHistory.clear()
            conversationHistory.add(content("user") { text(userScenarioInput) })

            val initialMessages = listOf(Diagnosis.User(userScenarioInput))
            _uiState.value =
                DiagnosisUiState(
                    messages = initialMessages,
                    isShowingResults = true,
                    currentPhase = DiagnosisPhase.PROCESSING,
                    questionCount = 0,
                )

            generateFollowUpQuestions()
        }

        private fun generateFollowUpQuestions() {
            viewModelScope.launch {
                try {
                    addLoading()
                    Log.d("Diagnosis_LOG", "[VM] 추가 질문 요청 중... language: $capturedLanguage")

                    val followUpAction =
                        getFollowUpQuestionUseCase(
                            history = conversationHistory,
                            questionCount = _uiState.value.questionCount,
                            language = capturedLanguage,
                        )
                    removeLoadingOnly()

                    if (followUpAction.isEnough) {
                        Log.d("Diagnosis_LOG", "[VM] 정보 충분 → 최종 분석 실행")
                        executeDiagnosisPipeline()
                    } else {
                        // AI 질문을 model role로 history에 추가
                        conversationHistory.add(content("model") { text(followUpAction.question) })

                        val currentMessages = _uiState.value.messages.toMutableList()
                        currentMessages.add(Diagnosis.BotWithOptions(followUpAction.question, followUpAction.options))

                        _uiState.value =
                            _uiState.value.copy(
                                messages = currentMessages,
                                currentPhase = DiagnosisPhase.AWAITING_ANSWERS,
                                questionCount = _uiState.value.questionCount + 1,
                            )
                    }
                } catch (e: Exception) {
                    removeLoadingOnly()
                    val errorMsg = resolveErrorMessage(e)
                    Log.e("Diagnosis_LOG", "[VM] 추가 질문 실패: ${e.javaClass.simpleName} - ${e.message}")
                    showRetryError(RetryActionType.FOLLOW_UP_QUESTIONS, errorMsg)
                }
            }
        }

        fun handleUserAnswerToQuestions(text: String) {
            if (text.isBlank()) return

            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(Diagnosis.User(text))
            conversationHistory.add(content("user") { text(text) })

            _uiState.value =
                _uiState.value.copy(
                    messages = currentMessages,
                    currentPhase = DiagnosisPhase.PROCESSING,
                )
            generateFollowUpQuestions()
        }

        private fun executeDiagnosisPipeline() {
            viewModelScope.launch {
                try {
                    addLoading()
                    Log.d("Diagnosis_LOG", "[VM] 최종 가이드 스트리밍 시작 - language: $capturedLanguage")
                    Log.d("Diagnosis_LOG", "[VM] 최종 가이드 스트리밍 시작")
                    Log.d("Diagnosis_LOG", "[VM] 최종 가이드 생성 시작")

                    val finalGuide =
                        generateDiagnosisGuideUseCase(
                            history = conversationHistory,
                            language = capturedLanguage,
                            onChunk = { chunk ->
                                if (_uiState.value.streamingText.isEmpty()) {
                                    removeLoadingOnly()
                                }
                                _uiState.value =
                                    _uiState.value.copy(
                                        streamingText = _uiState.value.streamingText + chunk,
                                    )
                            },
                        )

                    val currentMessages = _uiState.value.messages.toMutableList()
                    currentMessages.add(Diagnosis.Bot(finalGuide))

                    _uiState.value =
                        _uiState.value.copy(
                            messages = currentMessages,
                            currentPhase = DiagnosisPhase.IDLE,
                            streamingText = "",
                        )
                    Log.d("Diagnosis_LOG", "[VM] 전체 파이프라인 완료")
                } catch (e: Exception) {
                    removeLoadingOnly()
                    _uiState.value = _uiState.value.copy(streamingText = "")
                    val errorMsg = resolveErrorMessage(e)
                    Log.e("Diagnosis_LOG", "[VM] 파이프라인 실패: ${e.javaClass.simpleName} - ${e.message}")
                    showRetryError(RetryActionType.FINAL_GUIDE, errorMsg)
                }
            }
        }

        private fun resolveErrorMessage(e: Exception): String =
            when {
                e is kotlinx.coroutines.TimeoutCancellationException ->
                    "AI 응답 시간이 초과되었습니다.\n잠시 후 다시 시도해주세요."

                e.message?.contains("429") == true || e.message?.contains("quota") == true ->
                    "AI 사용량 한도를 초과했습니다.\n잠시 후 다시 시도해주세요."

                e.message?.contains("401") == true || e.message?.contains("403") == true ->
                    "API 인증에 실패했습니다.\n앱을 재시작해주세요."

                e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("timeout") == true ->
                    "네트워크 연결을 확인해주세요."

                else ->
                    "분석이 일시 중단되었습니다.\n잠시 후 다시 요청해주세요."
            }

        private fun showRetryError(
            type: RetryActionType,
            message: String = "분석이 일시 중단되었습니다.\n잠시 후 다시 요청해주세요.",
        ) {
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(Diagnosis.ErrorRetry(message, type))
            _uiState.value =
                _uiState.value.copy(
                    messages = currentMessages,
                    currentPhase = DiagnosisPhase.IDLE,
                )
        }

        fun retryAction(type: RetryActionType) {
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.removeAll { it is Diagnosis.ErrorRetry }
            _uiState.value =
                _uiState.value.copy(
                    messages = currentMessages,
                    currentPhase = DiagnosisPhase.PROCESSING,
                    streamingText = "",
                )

            when (type) {
                RetryActionType.FOLLOW_UP_QUESTIONS -> generateFollowUpQuestions()
                RetryActionType.FINAL_GUIDE -> executeDiagnosisPipeline()
            }
        }

        private fun addLoading() {
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(Diagnosis.Loading)
            _uiState.value = _uiState.value.copy(messages = currentMessages)
        }

        private fun removeLoadingOnly() {
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.removeAll { it is Diagnosis.Loading }
            _uiState.value = _uiState.value.copy(messages = currentMessages)
        }

        fun resetDiagnosis() {
            conversationHistory.clear()
            userScenarioInput = ""
            capturedLanguage = "ko"
            _uiState.value = DiagnosisUiState()
        }
    }
