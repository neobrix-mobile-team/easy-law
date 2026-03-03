package com.easylaw.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.datasource.PrecedentService
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.util.KeywordOptimizer
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

data class SearchParams(
    val query: String,
    val orgCode: String?,
)

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

//    private val _searchParams = MutableStateFlow<SearchParams?>(null)

//        val searchResults: Flow<PagingData<Precedent>> =
//            _searchParams
//                .filterNotNull()
//                .flatMapLatest { params ->
//                    repository.getPrecedentsStream(
//                        query = params.query,
//                        org = params.orgCode,
//                        onTotalCountFetched = { totalCnt ->
//                            _uiState.update { it.copy(totalSearchCount = totalCnt) }
//                        },
//                    )
//                }.cachedIn(viewModelScope)
//                .combine(_listFilterText) { pagingData, filterQuery ->
//                    if (filterQuery.isBlank()) {
//                        pagingData
//                    } else {
//                        pagingData.filter { precedent ->
//                            precedent.title.contains(filterQuery, ignoreCase = true) ||
//                                precedent.category.contains(filterQuery, ignoreCase = true) ||
//                                precedent.judgmentType.contains(filterQuery, ignoreCase = true)
//                        }
//                    }
//                }.cachedIn(viewModelScope)
//                .stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

        val _searchResults = MutableStateFlow<List<Precedent>>(emptyList())
//    val searchResults: StateFlow<List<Precedent>> = _searchResults.asStateFlow()

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
//        _uiState.update { it.copy(listFilterText = "") }

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
//            _searchParams.value = SearchParams(query = rawKeyword, orgCode = currentState.selectedCourt.orgCode)
                fetchPrecedentsList(rawKeyword, currentState.selectedCourt.orgCode)
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

//                    _searchParams.value =
//                        SearchParams(
//                            query = keyword,
//                            orgCode = currentState.selectedCourt.orgCode,
//                        )
                        fetchPrecedentsList(keyword, currentState.selectedCourt.orgCode)
                    } catch (e: Exception) {
                        Log.e("searchLegalAdvice failed", "키워드 추출 실패: $e")
                        _uiState.update { it.copy(isLoadingGemini = false) }
                    }
                }
            }
        }

        private fun fetchPrecedentsList(
            query: String,
            orgCode: String?,
        ) {
//        viewModelScope.launch {
//            val (totalCount, list) = repository.getPrecedents(query = query, org = orgCode)
//
//            _uiState.update { it.copy(totalSearchCount = totalCount) }
//            _searchResults.value = list
//        }

            searchJob?.cancel()

            searchJob =
                viewModelScope.launch {
                    var currentPage = 1
                    val displaySize = 100
                    var isFetching = true

                    while (isFetching) {
                        val (totalCount, list) =
                            repository.getPrecedents(
                                query = query,
                                org = orgCode,
                                page = currentPage,
                                display = displaySize,
                            )

                        if (currentPage == 1) {
                            // 첫 페이지는 즉시 화면에 노출하기 위해 바로 업데이트
                            _uiState.update { it.copy(totalSearchCount = totalCount, isLoading = true) }
                            _searchResults.value = list
                        } else {
                            // 두 번째 페이지부터는 기존 데이터 뒤에 새 데이터를 병합(누적)
                            _searchResults.update { currentList -> currentList + list }
                        }

                        if (list.isEmpty() || _searchResults.value.size >= totalCount) {
                            _uiState.update { it.copy(isLoading = false) }
                            isFetching = false
                            Log.d("LegalSearchViewModel", "모든 검색 결과 로딩 완료 (${_searchResults.value.size}건)")
                        } else {
                            currentPage++
                            // 다음 호출 전 0.5초 대기
//                    delay(500)
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
