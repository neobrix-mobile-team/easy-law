package com.easylaw.app.ui.screen.lawConsult

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.dto.precModel
import com.easylaw.app.repository.AIRepo
import com.easylaw.app.repository.LawApiRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LawConsultViewState(
    val situationInput: String = "",
    val lawTypeInput: String = "",
    val detailInput: String = "",
    val precList: List<precModel> = emptyList(),
    val precSuccess: Boolean = false,
)

// 판례검색 화면 뷰 모델
@HiltViewModel
class LawConsultViewModel
    @Inject
    constructor(
        private val lawApiRepo: LawApiRepo,
//    private val generativeModel: GenerativeModel,
        private val aiManager: AIRepo,
    ) : ViewModel() {
        val _lawConsultViewState = MutableStateFlow(LawConsultViewState())
        val lawConsultViewState = _lawConsultViewState.asStateFlow()

        fun onChangedTextField(newState: LawConsultViewState) {
            _lawConsultViewState.value = newState
        }

        fun onDismiss() {
            _lawConsultViewState.update {
                    it ->
                it.copy(
                    precSuccess = false,
                    precList = emptyList(),
                )
            }
        }

        fun searchPrecedents() {
            viewModelScope.launch {
                try {
                    _lawConsultViewState.update { it ->
                        it.copy(
                            precList = emptyList(),
                            precSuccess = false,
                        )
                    }

                    // 상황 input
                    val rawSituation = _lawConsultViewState.value.situationInput
                    // 상세 input
                    val rawDetail = _lawConsultViewState.value.detailInput

                    val prompt =
                        """
                        당신은 법률 전문가입니다. 다음 내용을 분석하여 한국 판례 검색 API(LISC)에 
                        가장 적합한 핵심 법률 명사 딱 1개만 뽑아주세요.
                        상황: $rawSituation
                        상세: $rawDetail
                        결과는 반드시 단어 하나만 대답하세요.
                        """.trimIndent()

                    val response = aiManager.execute(prompt)

                    val res =
                        lawApiRepo.getPrecedents(
                            query = response,
                            curt = _lawConsultViewState.value.lawTypeInput,
                        )

                    val prec = res?.precSearch?.prec

                    _lawConsultViewState.update { it ->
                        it.copy(
                            precList = prec ?: emptyList(),
                            precSuccess = true,
                        )
                    }
                } catch (e: Exception) {
                    Log.e("searchPrecedents error", "searchPrecedents: ${e.message}")
                }
            }
        }
    }
