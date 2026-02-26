package com.easylaw.app.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.easylaw.app.data.datasource.PrecedentService
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.util.KeywordOptimizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchParams(
    val query: String,
    val orgCode: String?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LegalSearchViewModel
    @Inject
    constructor(
        private val repository: LawRepository,
        private val precedentService: PrecedentService,
        private val keywordOptimizer: KeywordOptimizer,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LegalSearchUiState())
        val uiState: StateFlow<LegalSearchUiState> = _uiState.asStateFlow()

        private val _searchParams = MutableStateFlow<SearchParams?>(null)

        val searchResults: Flow<PagingData<Precedent>> =
            _searchParams
                .filterNotNull()
                .flatMapLatest { params ->
                    repository.getPrecedentsStream(
                        query = params.query,
                        org = params.orgCode,
                        onTotalCountFetched = { totalCnt ->
                            _uiState.update { it.copy(totalSearchCount = totalCnt) }
                        },
                    )
                }.cachedIn(viewModelScope)
                .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

        fun updateSituation(newSituation: String) {
            _uiState.update { it.copy(situation = newSituation, isSituationError = newSituation.isBlank()) }
        }

        fun updateCourtType(courtOption: CourtTypeOption) {
            _uiState.update { it.copy(selectedCourt = courtOption) }
        }

        fun updateDetails(newDetails: String) {
            _uiState.update { it.copy(details = newDetails) }
        }

        fun closeResults() {
            _uiState.update { it.copy(showResults = false) }
        }

        fun searchLegalAdvice() {
            val currentState = _uiState.value

            if (currentState.situation.isBlank()) {
                _uiState.update { it.copy(isSituationError = true) }
                return
            }

            val canBypass = keywordOptimizer.shouldBypassGemini(currentState.situation, currentState.details)

            if (canBypass) {
                val rawKeyword = currentState.situation.trim()

                _uiState.update {
                    it.copy(
                        isSituationError = false,
                        totalSearchCount = 0,
                        showResults = true,
                        isLoadingGemini = false, // AI 로딩 화면 생략
                        extractedKeyword = rawKeyword, // 원본 단어 그대로 표출
                    )
                }

                // 바로 페이징 검색 트리거
                _searchParams.value = SearchParams(query = rawKeyword, orgCode = currentState.selectedCourt.orgCode)
            } else {
                // 키워드 분석
                _uiState.update { it.copy(isLoadingGemini = true, isSituationError = false, totalSearchCount = 0) }

                viewModelScope.launch {
                    try {
                        // Gemini를 활용하여 긴 문장을 핵심 키워드로 압축 (API 통신 실패 방지)
                        val keyword = precedentService.extractKeyword(currentState.situation, currentState.details)

                        // 상태 업데이트 및 Paging 트리거 발동
                        _uiState.update {
                            it.copy(
                                isLoadingGemini = false,
                                showResults = true,
                                extractedKeyword = keyword,
                            )
                        }

                        _searchParams.value =
                            SearchParams(
                                query = keyword,
                                orgCode = currentState.selectedCourt.orgCode,
                            )
                    } catch (e: Exception) {
                        Log.e("searchLegalAdvice failed", "키워드 추출 실패: $e")
                        _uiState.update { it.copy(isLoadingGemini = false) }
                    }
                }
            }
        }

        fun onPrecedentClick(precedent: Precedent) {
            _uiState.update {
                it.copy(
                    showDetailDialog = true,
                    isDetailLoading = true,
                    detailViewMode = DetailViewMode.ORIGINAL,
                    summaryText = "",
                    selectedPrecedentLink = precedent.detailLink,
                )
            }

            viewModelScope.launch {
                val detail = repository.getPrecedentDetail(precedent.id)
                _uiState.update { it.copy(currentPrecedentDetail = detail, isDetailLoading = false) }
            }
        }

        fun closeDetailDialog() {
            _uiState.update { it.copy(showDetailDialog = false, currentPrecedentDetail = null) }
        }

        fun toggleDetailViewMode(mode: DetailViewMode) {
            _uiState.update { it.copy(detailViewMode = mode) }

            val currentState = _uiState.value

            if (mode == DetailViewMode.SUMMARY && currentState.summaryText.isEmpty()) {
                val originalText = currentState.currentPrecedentDetail?.fullTextForAi ?: return

                _uiState.update { it.copy(isSummaryLoading = true) }

                viewModelScope.launch {
                    val summary = precedentService.summarizePrecedent(originalText)
                    _uiState.update { it.copy(summaryText = summary, isSummaryLoading = false) }
                }
            }
        }
    }
