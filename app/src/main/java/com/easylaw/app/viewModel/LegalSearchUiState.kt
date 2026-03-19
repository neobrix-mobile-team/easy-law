package com.easylaw.app.viewModel

import androidx.annotation.StringRes
import com.easylaw.app.R
import com.easylaw.app.domain.model.PrecedentDetail

enum class CourtTypeOption(
    @StringRes val displayName: Int,
    val orgCode: String?,
) {
    ALL(R.string.court_all, null),
    SUPREME(R.string.court_supreme, "400201"),
    LOWER(R.string.court_lower, "400202"),
}

enum class DetailViewMode {
    ORIGINAL,
    SUMMARY,
}

data class LegalSearchUiState(
    // 목록
    val situation: String = "",
    val selectedCourt: CourtTypeOption = CourtTypeOption.ALL,
    val details: String = "",
    val isSituationError: Boolean = false,
    val showResults: Boolean = false,
    val isLoading: Boolean = false, // 판례 목록 API 로딩
    val isLoadingGemini: Boolean = false,
    val extractedKeyword: String = "",
    val totalSearchCount: Int = 0,
    val listFilterText: String = "",
    val translatedTitles: Map<String, String> = emptyMap(),
    // 본문
    val showDetailDialog: Boolean = false, // 상세 팝업 노출 여부
    val detailViewMode: DetailViewMode = DetailViewMode.ORIGINAL, // 원문 or 요약 모드
    val currentPrecedentDetail: PrecedentDetail? = null, // 상세 API로 받아온 원본 데이터
    val selectedPrecedentLink: String = "",
    val isDetailLoading: Boolean = false, // 상세 API 로딩
    val detailTitle: String = "",
    val summaryText: String = "", // 요약본
    val isSummaryLoading: Boolean = false, // 요약 로딩
    val streamingSummaryText: String = "",
)
