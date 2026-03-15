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
        private val getFollowUpQuestionUseCase: GetFollowUpQuestionUseCase,
        private val generateDiagnosisGuideUseCase: GenerateDiagnosisGuideUseCase,
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
//            val firstScenario = conversationHistory.firstOrNull() ?: ""
//            // 최신 질문과 답변 세트(최대 2개)만 잘라서 가져옵니다.
//            val recentConversations =
//                if (conversationHistory.size > 1) {
//                    conversationHistory.takeLast(2).joinToString(" ")
//                } else {
//                    ""
//                }
//            return if (recentConversations.isNotEmpty()) "$firstScenario $recentConversations" else firstScenario

            if (conversationHistory.isEmpty()) return ""
            return conversationHistory.joinToString("\n")
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
                try {
                    addLoading()
                    Log.d("Diagnosis_LOG", "[VM] 추가 질문 요청 중...")

                    // UseCase가 질문 횟수 제한 비즈니스 로직을 내부에서 처리
                    val followUpAction =
                        getFollowUpQuestionUseCase(
                            scenario = scenario,
                            questionCount = _uiState.value.questionCount,
                        )
                    removeLoadingOnly()

                    if (followUpAction.isEnough) {
                        Log.d("Diagnosis_LOG", "[VM] 정보 충분 → 최종 분석 실행")
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

                    Log.d("Diagnosis_LOG", "[VM] 최종 가이드 생성 시작")
                    // UseCase가 법령추출 → 조회 → 가이드생성 파이프라인을 캡슐화
                    val finalGuide = generateDiagnosisGuideUseCase(contextForAnalysis)
                    removeLoadingOnly()

                    val currentMessages = _uiState.value.messages.toMutableList()
                    currentMessages.add(Diagnosis.Bot(finalGuide))

                    _uiState.value =
                        _uiState.value.copy(
                            messages = currentMessages,
                            currentPhase = DiagnosisPhase.IDLE,
                        )
                    Log.d("Diagnosis_LOG", "[VM] 전체 파이프라인 완료")
                } catch (e: Exception) {
                    removeLoadingOnly()
                    val errorMsg = resolveErrorMessage(e)
                    Log.e("Diagnosis_LOG", "[VM] 파이프라인 실패: ${e.javaClass.simpleName} - ${e.message}")
                    showRetryError(RetryActionType.FINAL_GUIDE, errorMsg)
                }
            }
        }

        // 에러 원인에 따라 사용자에게 보여줄 메시지를 결정
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
