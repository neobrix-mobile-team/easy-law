package com.easylaw.app.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.domain.usecase.SearchPrecedentsUseCase
import com.easylaw.app.domain.usecase.SummarizePrecedentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LegalSearchViewModel
    @Inject
    constructor(
        private val searchPrecedentsUseCase: SearchPrecedentsUseCase,
        private val summarizePrecedentUseCase: SummarizePrecedentUseCase,
        private val lawRepository: LawRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LegalSearchUiState())
        val uiState: StateFlow<LegalSearchUiState> = _uiState.asStateFlow()

        private val _searchResults = MutableStateFlow<List<Precedent>>(emptyList())

        private val _filterKeyword = MutableStateFlow("")
        val filterKeyword: StateFlow<String> = _filterKeyword.asStateFlow()

        private var searchJob: Job? = null

        val displayResults: StateFlow<List<Precedent>> =
            combine(
                _searchResults,
                _filterKeyword,
            ) { list, keyword ->
                if (keyword.isBlank()) {
                    list
                } else {
                    list.filter { precedent ->
                        precedent.title.contains(keyword, ignoreCase = true) ||
                            precedent.category.contains(keyword, ignoreCase = true) ||
                            precedent.court.contains(keyword, ignoreCase = true) ||
                            precedent.date.contains(keyword, ignoreCase = true) ||
                            precedent.judgmentType.contains(keyword, ignoreCase = true)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

        fun updateSituation(newSituation: String) {
            _uiState.update { it.copy(situation = newSituation, isSituationError = newSituation.isBlank()) }
        }

        fun updateCourtType(courtOption: CourtTypeOption) {
            _uiState.update { it.copy(selectedCourt = courtOption) }
        }

        fun updateDetails(newDetails: String) {
            _uiState.update { it.copy(details = newDetails) }
        }

        fun updateListFilterText(text: String) {
            _filterKeyword.value = text
            _uiState.update { it.copy(listFilterText = text) }
        }

        fun closeResults() {
            _uiState.update { it.copy(showResults = false) }
            _filterKeyword.value = ""
            searchJob?.cancel()
        }

        fun searchLegalAdvice() {
            val currentState = _uiState.value

            if (currentState.situation.isBlank()) {
                _uiState.update { it.copy(isSituationError = true) }
                return
            }

            _searchResults.value = emptyList()
            _filterKeyword.value = ""

            viewModelScope.launch {
                // UseCase가 "Gemini 우회 판단 + 키워드 추출" 로직을 캡슐화
                val isAiNeeded =
                    !com.easylaw.app.util.KeywordOptimizer().shouldBypassGemini(
                        currentState.situation,
                        currentState.details,
                    )

                if (isAiNeeded) {
                    _uiState.update { it.copy(isLoadingGemini = true, isSituationError = false, totalSearchCount = 0) }
                }

                try {
                    Log.d("LegalSearch_LOG", "[키워드 추출] 시작")
                    val resolution =
                        searchPrecedentsUseCase.resolveKeyword(
                            currentState.situation,
                            currentState.details,
                        )
                    Log.d("LegalSearch_LOG", "[키워드 추출] 완료: ${resolution.keyword}, AI사용: ${resolution.wasOptimizedByAi}")

                    _uiState.update {
                        it.copy(
                            isLoadingGemini = false,
                            isSituationError = false,
                            totalSearchCount = 0,
                            showResults = true,
                            extractedKeyword = resolution.keyword,
                        )
                    }

                    fetchPrecedentsList(resolution.keyword, currentState.selectedCourt.orgCode)
                } catch (e: Exception) {
                    Log.e("LegalSearch_LOG", "[키워드 추출] 실패: ${e.javaClass.simpleName} - ${e.message}")
                    _uiState.update { it.copy(isLoadingGemini = false) }
                }
            }
        }

        private fun fetchPrecedentsList(
            query: String,
            orgCode: String?,
        ) {
            searchJob?.cancel()

            searchJob =
                viewModelScope.launch {
                    var currentPage = 1
                    val displaySize = 100
                    var isFetching = true

                    while (isFetching) {
                        lawRepository
                            .getPrecedents(
                                query = query,
                                org = orgCode,
                                page = currentPage,
                                display = displaySize,
                            ).onSuccess { result ->
                                if (currentPage == 1) {
                                    _uiState.update { it.copy(totalSearchCount = result.totalCount, isLoading = true) }
                                    _searchResults.value = result.items
                                } else {
                                    _searchResults.update { currentList -> currentList + result.items }
                                }

                                if (result.items.isEmpty() || _searchResults.value.size >= result.totalCount) {
                                    _uiState.update { it.copy(isLoading = false) }
                                    isFetching = false
                                    Log.d("LegalSearchViewModel", "모든 검색 결과 로딩 완료 (${_searchResults.value.size}건)")
                                } else {
                                    currentPage++
                                }
                            }.onFailure { e ->
                                // 이전: 에러 발생해도 알 수 없었음 (빈 리스트 반환)
                                // 수정: 에러 상태를 UI에 반영 가능
                                Log.e("LegalSearchViewModel", "판례 조회 실패 (페이지 $currentPage): ${e.message}")
                                _uiState.update { it.copy(isLoading = false) }
                                isFetching = false
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
                lawRepository
                    .getPrecedentDetail(precedent.id)
                    .onSuccess { detail ->
                        _uiState.update { it.copy(currentPrecedentDetail = detail, isDetailLoading = false) }
                    }.onFailure {
                        _uiState.update { it.copy(isDetailLoading = false) }
                    }
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
                    try {
                        Log.d("LegalSearch_LOG", "[판례 요약] 요청 시작")
                        // UseCase를 통해 간접 접근
                        val summary = summarizePrecedentUseCase(originalText)
                        Log.d("LegalSearch_LOG", "[판례 요약] 완료")
                        _uiState.update { it.copy(summaryText = summary, isSummaryLoading = false) }
                    } catch (e: Exception) {
                        Log.e("LegalSearch_LOG", "[판례 요약] 실패: ${e.javaClass.simpleName} - ${e.message}")
                        _uiState.update {
                            it.copy(
                                summaryText = "요약 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                isSummaryLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }
