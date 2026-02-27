package com.easylaw.app.viewmodel

import com.easylaw.app.domain.model.PrecedentDetail

enum class CourtTypeOption(
    val displayName: String,
    val orgCode: String?,
) {
    ALL("전체 (선택 안함)", null),
    SUPREME("대법원", "400201"),
    LOWER("하위법원", "400202"),
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
    val isLoadingGemini: Boolean = false,
    val extractedKeyword: String = "",
    val totalSearchCount: Int = 0,
    val listFilterText: String = "",
    // 본문
    val showDetailDialog: Boolean = false, // 상세 팝업 노출 여부
    val detailViewMode: DetailViewMode = DetailViewMode.ORIGINAL, // 원문 or 요약 모드
    val currentPrecedentDetail: PrecedentDetail? = null, // 상세 API로 받아온 원본 데이터
    val selectedPrecedentLink: String = "",
    val isDetailLoading: Boolean = false, // 상세 API 로딩
    val summaryText: String = "", // 요약본
    val isSummaryLoading: Boolean = false, // 요약 로딩
)
