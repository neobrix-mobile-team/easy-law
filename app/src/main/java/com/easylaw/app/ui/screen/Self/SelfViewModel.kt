package com.easylaw.app.ui.screen.Self

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.repository.AIRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SelfViewState(
    val situationInput: String = "",
    val selfList: List<String> = emptyList(),
    val selfSuccess: Boolean = false,
)

// 자가진단 화면 뷰 모델
@HiltViewModel
class SelfViewModel
    @Inject
    constructor(
        private val aiManager: AIRepo,
    ) : ViewModel() {
        val _selfViewState = MutableStateFlow(SelfViewState())
        val selfViewState = _selfViewState.asStateFlow()

        fun onChangedTextField(newState: SelfViewState) {
            _selfViewState.value = newState
        }

        fun searchSelf() {
            viewModelScope.launch {
                try {
                    _selfViewState.update {
                        it.copy(
                            selfList = emptyList(),
                            selfSuccess = false,
                        )
                    }

                    val rawSituation = _selfViewState.value.situationInput

                    val prompt =
                        """
                        당신은 어려운 법률 용어를 일반인의 눈높이에서 아주 쉽게 풀어주는 법률 가이드 AI입니다.
                        사용자의 상황을 분석하여 [진단명, 쉬운 풀이, 준비해야 할 증거] 순서로 답변해 주세요.
                        
                        [상황]
                        $rawSituation
                        
                        [출력 규칙]
                        1. 반드시 아래 3개의 항목만 리스트 형태로 답변하세요. (항목당 줄바꿈 1번)
                        2. 첫 번째 항목: 이 상황을 뜻하는 '핵심 법률 용어'
                        3. 두 번째 항목: 위 용어를 초등학생도 이해할 수 있게 비유를 들어 설명 (한 문장)
                        4. 세 번째 항목: 사용자가 지금 바로 확보해야 할 가장 중요한 '실질적 증거' 1가지
                        
                        [응답 예시]
                        부당해고
                        사장님이 정당한 이유 없이 나가라고 하는 것으로, 축구 경기 중에 이유 없이 퇴장 카드를 받은 것과 같아요.
                        해고 통지서 또는 사장님과의 대화 녹취록
                        """.trimIndent()

                    val responseText = aiManager.execute(prompt)

                    Log.d("자가진단 결과", responseText)

                    val resultList =
                        responseText
                            .split("\n")
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                    if (resultList.isNotEmpty()) {
                        _selfViewState.update {
                            it.copy(
                                selfList = resultList,
                                selfSuccess = true,
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("searchSelf error", "${e.message}")
                }
            }
        }
    }
