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

        private val conversationContext = StringBuilder()

        var userScenarioInput by mutableStateOf("")
            private set

        fun onUserScenarioInputChange(newValue: String) {
            userScenarioInput = newValue
        }

        fun onStartDiagnosis() {
            if (userScenarioInput.isBlank()) return

            conversationContext.clear()
            conversationContext.append("사용자: $userScenarioInput\n")

            val initialMessages = listOf(Diagnosis.User(userScenarioInput))
            _uiState.value =
                DiagnosisUiState(
                    messages = initialMessages,
                    isShowingResults = true,
                    currentPhase = DiagnosisPhase.PROCESSING,
                    questionCount = 0,
                )

            generateFollowUpQuestions(conversationContext.toString())
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
                        conversationContext.append("시스템: ${followUpAction.question}\n")

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
            conversationContext.append("사용자(답변): $text\n")

            _uiState.value =
                _uiState.value.copy(
                    messages = currentMessages,
                    currentPhase = DiagnosisPhase.PROCESSING,
                )
            generateFollowUpQuestions(conversationContext.toString())
        }

        private fun executeDiagnosisPipeline() {
            viewModelScope.launch {
                try {
                    addLoading()

                    val lawNames = repository.extractTargetLaws(conversationContext.toString())
                    val lawDetails = repository.fetchDiagnosisDetails(lawNames)
                    val finalGuide = repository.generateFinalGuide(conversationContext.toString(), lawDetails)

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

        // [수정] 사용자가 "분석요청" 버튼을 누르면 에러 메시지를 지우고 멈췄던 로직을 다시 실행합니다.
        fun retryAction(type: RetryActionType) {
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.removeAll { it is Diagnosis.ErrorRetry }
            _uiState.value =
                _uiState.value.copy(
                    messages = currentMessages,
                    currentPhase = DiagnosisPhase.PROCESSING,
                )

            when (type) {
                RetryActionType.FOLLOW_UP_QUESTIONS -> generateFollowUpQuestions(conversationContext.toString())
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
