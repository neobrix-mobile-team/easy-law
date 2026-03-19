package com.easylaw.app.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.data.repository.PrecedentLabelTranslation
import com.easylaw.app.data.repository.TranslationRepository
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.domain.usecase.SearchPrecedentsUseCase
import com.easylaw.app.domain.usecase.SummarizePrecedentUseCase
import com.easylaw.app.util.PreferenceManager
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
        private val translationRepository: TranslationRepository,
        private val preferenceManager: PreferenceManager,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(LegalSearchUiState())
        val uiState: StateFlow<LegalSearchUiState> = _uiState.asStateFlow()

        private val _searchResults = MutableStateFlow<List<Precedent>>(emptyList())
        private val _filterKeyword = MutableStateFlow("")
        val filterKeyword: StateFlow<String> = _filterKeyword.asStateFlow()

        private var searchJob: Job? = null

        private val translatingIds = mutableSetOf<String>()

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
            translatingIds.clear()
            _uiState.update { it.copy(translatedTitles = emptyMap()) }

            viewModelScope.launch {
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

                    fetchPrecedentsFromKeywords(resolution.keywords, currentState.selectedCourt.orgCode)
                } catch (e: Exception) {
                    Log.e("LegalSearch_LOG", "[키워드 추출] 실패: ${e.javaClass.simpleName} - ${e.message}")
                    _uiState.update { it.copy(isLoadingGemini = false) }
                }
            }
        }

        private fun fetchPrecedentsFromKeywords(
            keywords: List<String>,
            orgCode: String?,
        ) {
            searchJob?.cancel()

            searchJob =
                viewModelScope.launch {
                    val seenIds = mutableSetOf<String>()

                    keywords.forEachIndexed { index, keyword ->
                        Log.d("LegalSearchViewModel", "[검색 시작] 키워드: \"$keyword\" (${index + 1}/${keywords.size})")
                        var currentPage = 1
                        val displaySize = 100
                        var isFetching = true

                        while (isFetching) {
                            lawRepository
                                .getPrecedents(
                                    query = keyword,
                                    org = orgCode,
                                    page = currentPage,
                                    display = displaySize,
                                ).onSuccess { result ->
                                    val newItems = result.items.filter { seenIds.add(it.id) }

                                    if (index == 0 && currentPage == 1) {
                                        _uiState.update { it.copy(totalSearchCount = result.totalCount, isLoading = true) }
                                        _searchResults.value = newItems
                                    } else {
                                        _uiState.update { it.copy(totalSearchCount = it.totalSearchCount + result.totalCount) }
                                        _searchResults.update { it + newItems }
                                    }

                                    if (result.items.isEmpty() || _searchResults.value.size >= result.totalCount) {
                                        isFetching = false
                                    } else {
                                        currentPage++
                                    }
                                }.onFailure { e ->
                                    Log.e("LegalSearchViewModel", "판례 조회 실패 (키워드: $keyword, 페이지 $currentPage): ${e.message}")
                                    isFetching = false
                                }
                        }
                    }

                    _uiState.update { it.copy(isLoading = false) }
                    Log.d("LegalSearchViewModel", "모든 검색 결과 로딩 완료 (${_searchResults.value.size}건)")
                }
        }

        fun translateVisibleItems(visiblePrecedents: List<Precedent>) {
            Log.d("Translation_LOG", "translateVisibleItems 호출 - ${visiblePrecedents.size}건, translatingIds: $translatingIds")
            val targetLang =
                PrecedentLabelTranslation.toDeepLLang(
                    preferenceManager.languageState.value,
                ) ?: run {
                    Log.d("Translation_LOG", "한국어 선택 → 번역 스킵")
                    return
                }
            val currentTranslations = _uiState.value.translatedTitles

            // 미번역 + 미진행 항목만 필터링
            val untranslated =
                visiblePrecedents.filter { precedent ->
                    !currentTranslations.containsKey(precedent.title) &&
                        !translatingIds.contains(precedent.id)
                }

            Log.d("Translation_LOG", "미번역 항목: ${untranslated.size}건, targetLang: $targetLang")

            if (untranslated.isEmpty()) return

            val labelTranslations =
                untranslated
                    .flatMap { precedent ->
                        listOf(
                            precedent.category to PrecedentLabelTranslation.translateCategory(precedent.category, targetLang),
                            precedent.court to PrecedentLabelTranslation.translateCategory(precedent.court, targetLang),
                            precedent.judgmentType to PrecedentLabelTranslation.translateCategory(precedent.judgmentType, targetLang),
                        )
                    }.filter { (original, translated) -> original.isNotEmpty() && original != translated }
                    .toMap()

            if (labelTranslations.isNotEmpty()) {
                _uiState.update { state ->
                    state.copy(translatedTitles = state.translatedTitles + labelTranslations)
                }
            }

            // 진행 중 표시 (중복 요청 방지)
            untranslated.forEach { translatingIds.add(it.id) }

            viewModelScope.launch {
                val titles = untranslated.map { it.title }
                Log.d("Translation_LOG", "[번역] DeepL 요청 - ${titles.size}건")

                val translated = translationRepository.translateTitles(titles, targetLang)

                Log.d("Translation_LOG", "DeepL 응답: $translated")

                _uiState.update { state ->
                    state.copy(
                        translatedTitles = state.translatedTitles + translated,
                    )
                }

                Log.d("Translation_LOG", "캐시 저장 후 총 번역 수: ${_uiState.value.translatedTitles.size}")

                // 완료된 항목을 진행 중 목록에서 제거
                untranslated.forEach { translatingIds.remove(it.id) }

                Log.d("Translation_LOG", "[번역] 완료 - ${translated.size}건 누적: ${_uiState.value.translatedTitles.size}건")
            }
        }

        fun onPrecedentClick(precedent: Precedent) {
            val language = preferenceManager.languageState.value
            val isNonKorean = language != "ko"

            val displayTitle =
                if (isNonKorean) {
                    _uiState.value.translatedTitles[precedent.title] ?: precedent.title
                } else {
                    precedent.title
                }

            _uiState.update {
                it.copy(
                    showDetailDialog = true,
                    isDetailLoading = true,
                    detailViewMode = if (isNonKorean) DetailViewMode.SUMMARY else DetailViewMode.ORIGINAL,
                    detailTitle = displayTitle,
                    summaryText = "",
                    streamingSummaryText = "",
                    selectedPrecedentLink = precedent.detailLink,
                    isSummaryLoading = isNonKorean,
                )
            }

            viewModelScope.launch {
                lawRepository
                    .getPrecedentDetail(precedent.id)
                    .onSuccess { detail ->
                        _uiState.update { it.copy(currentPrecedentDetail = detail, isDetailLoading = false) }

                        if (isNonKorean) {
                            startSummary(detail.fullTextForAi, language)
                        }
                    }.onFailure {
                        _uiState.update { it.copy(isDetailLoading = false) }
                    }
            }
        }

        private fun startSummary(
            originalText: String,
            language: String,
        ) {
            _uiState.update { it.copy(isSummaryLoading = true, streamingSummaryText = "") }

            viewModelScope.launch {
                try {
                    Log.d("LegalSearch_LOG", "[판례 요약] 요청 시작 - 언어: $language")
                    val summary =
                        summarizePrecedentUseCase(
                            originalText,
                            language,
                            onChunk = { chunk ->
                                if (_uiState.value.streamingSummaryText.isEmpty()) {
                                    _uiState.update { it.copy(isSummaryLoading = false) }
                                }
                                _uiState.update { it.copy(streamingSummaryText = it.streamingSummaryText + chunk) }
                            },
                        )
                    Log.d("LegalSearch_LOG", "[판례 요약] 완료")
                    _uiState.update { it.copy(summaryText = summary, streamingSummaryText = "", isSummaryLoading = false) }
                } catch (e: Exception) {
                    Log.e("LegalSearch_LOG", "[판례 요약] 실패: ${e.javaClass.simpleName} - ${e.message}")
                    _uiState.update {
                        it.copy(
                            summaryText = "요약 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                            streamingSummaryText = "",
                            isSummaryLoading = false,
                        )
                    }
                }
            }
        }

        fun closeDetailDialog() {
            _uiState.update {
                it.copy(showDetailDialog = false, currentPrecedentDetail = null, streamingSummaryText = "", detailTitle = "")
            }
        }

        fun toggleDetailViewMode(mode: DetailViewMode) {
            _uiState.update { it.copy(detailViewMode = mode) }

            val currentState = _uiState.value

            // 요약 탭 수동 클릭 시 - 아직 요약이 없고 상세 데이터가 있을 때만 실행
            if (mode == DetailViewMode.SUMMARY &&
                currentState.summaryText.isEmpty() &&
                !currentState.isSummaryLoading
            ) {
                val originalText = currentState.currentPrecedentDetail?.fullTextForAi ?: return
                val language = preferenceManager.languageState.value
                startSummary(originalText, language)
            }
        }
    }
