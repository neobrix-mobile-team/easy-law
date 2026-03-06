package com.easylaw.app.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.repository.DiagnosisRepository
import com.easylaw.app.domain.model.Diagnosis
import com.easylaw.app.domain.model.DiagnosisPhase
import com.easylaw.app.domain.model.RetryActionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosisUiState(
    val messages: List<Diagnosis> = emptyList(),
    val currentPhase: DiagnosisPhase = DiagnosisPhase.IDLE,
    val isShowingResults: Boolean = false,
    val questionCount: Int = 0,
)

@HiltViewModel
class DiagnosisViewModel
    @Inject
    constructor(
        private val repository: DiagnosisRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(DiagnosisUiState())
        val uiState: StateFlow<DiagnosisUiState> = _uiState.asStateFlow()

        private val conversationHistory = mutableListOf<String>()

        var userScenarioInput by mutableStateOf("")
            private set

        fun onUserScenarioInputChange(newValue: String) {
            userScenarioInput = newValue
        }

        private fun getOptimizedContext(): String {
            val firstScenario = conversationHistory.firstOrNull() ?: ""
            // 최신 질문과 답변 세트(최대 2개)만 잘라서 가져옵니다.
            val recentConversations =
                if (conversationHistory.size > 1) {
                    conversationHistory.takeLast(2).joinToString(" ")
                } else {
                    ""
                }
            return if (recentConversations.isNotEmpty()) "$firstScenario $recentConversations" else firstScenario
        }

        fun onStartDiagnosis() {
            if (userScenarioInput.isBlank()) return

            conversationHistory.clear()
            conversationHistory.add("최초상황: $userScenarioInput")

            val initialMessages = listOf(Diagnosis.User(userScenarioInput))
            _uiState.value =
                DiagnosisUiState(
                    messages = initialMessages,
                    isShowingResults = true,
                    currentPhase = DiagnosisPhase.PROCESSING,
                    questionCount = 0,
                )

            generateFollowUpQuestions(getOptimizedContext())
        }

        private fun generateFollowUpQuestions(scenario: String) {
            viewModelScope.launch {
                if (_uiState.value.questionCount >= 3) {
                    executeDiagnosisPipeline()
                    return@launch
                }

                try {
                    addLoading()
                    val followUpAction = repository.getAdditionalQuestions(scenario)
                    removeLoadingOnly()

                    if (followUpAction.isEnough) {
                        executeDiagnosisPipeline()
                    } else {
                        conversationHistory.add("시스템질문: ${followUpAction.question}")

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
                    Log.e("ERROR", "generateFollowUpQuestions error $e", e)
                    showRetryError(RetryActionType.FOLLOW_UP_QUESTIONS)
                }
            }
        }

        fun handleUserAnswerToQuestions(text: String) {
            if (text.isBlank()) return

            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(Diagnosis.User(text))
            conversationHistory.add("추가답변: $text")

            _uiState.value =
                _uiState.value.copy(
                    messages = currentMessages,
                    currentPhase = DiagnosisPhase.PROCESSING,
                )
            generateFollowUpQuestions(getOptimizedContext())
        }

        private fun executeDiagnosisPipeline() {
            viewModelScope.launch {
                try {
                    addLoading()
                    val contextForAnalysis = getOptimizedContext()
                    val lawNames = repository.extractTargetLaws(contextForAnalysis)
                    val lawDetails = repository.fetchDiagnosisDetails(lawNames)
                    val finalGuide = repository.generateFinalGuide(contextForAnalysis, lawDetails)

                    removeLoadingOnly()

                    val currentMessages = _uiState.value.messages.toMutableList()
                    currentMessages.add(Diagnosis.Bot(finalGuide))

                    _uiState.value =
                        _uiState.value.copy(
                            messages = currentMessages,
                            currentPhase = DiagnosisPhase.IDLE,
                        )
                } catch (e: Exception) {
                    removeLoadingOnly()
                    showRetryError(RetryActionType.FINAL_GUIDE)
                }
            }
        }

        // 처음 사용된 구문 설명: 통신 에러가 발생했을 때 기존 메시지 리스트 하단에 "재시도" 버튼 UI 상태를 추가하는 함수입니다.
        private fun showRetryError(type: RetryActionType) {
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(Diagnosis.ErrorRetry("분석이 일시 중단되었습니다.\n잠시 후 다시 요청해주세요.", type))
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
                )

            when (type) {
                RetryActionType.FOLLOW_UP_QUESTIONS -> generateFollowUpQuestions(getOptimizedContext())
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
    }
