package com.easylaw.app.viewModel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.R
import com.easylaw.app.data.repository.MapRepository
import com.easylaw.app.domain.model.LawPlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LatLngPoint(
    val lat: Double,
    val lng: Double,
)

// 지도 필터 항목 정의
enum class MapFilter(
    @StringRes val labelResId: Int,
    val keyword: String?,
) {
    ALL(R.string.filter_all, null),
    COURT(R.string.filter_court, "법원"),
    PROSECUTOR(R.string.filter_prosecutor, "검찰"),
    POLICE(R.string.filter_police, "경찰"),
    LEGAL_AID(R.string.filter_legal_aid, "법률구조"),
    NOTARY(R.string.filter_notary, "공증"),
    REGISTRY(R.string.filter_registry, "등기"),
    JUDICIAL_SCRIVENER(R.string.filter_judicial_scrivener, "법무사"),
    LAWYER(R.string.filter_lawyer, "변호사"),
    LAW_FIRM(R.string.filter_law_firm, "법무법인"),
    PATENT_ATTORNEY(R.string.filter_patent_attorney, "변리사"),
}

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val mapRepository: MapRepository,
    ) : ViewModel() {
        private val _lawPlaces = MutableStateFlow<List<LawPlace>>(emptyList())

        private val _selectedPlace = MutableStateFlow<LawPlace?>(null)
        val selectedPlace: StateFlow<LawPlace?> = _selectedPlace.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        private val _currentLocation = MutableStateFlow<LatLngPoint?>(null)
        val currentLocation: StateFlow<LatLngPoint?> = _currentLocation.asStateFlow()

        // 선택된 필터 (기본값: 전체)
        private val _selectedFilter = MutableStateFlow(MapFilter.ALL)
        val selectedFilter: StateFlow<MapFilter> = _selectedFilter.asStateFlow()

        // 필터가 적용된 장소 목록 (UI에서 이걸 사용)
        val filteredPlaces: StateFlow<List<LawPlace>> =
            combine(_lawPlaces, _selectedFilter) { places, filter ->
                if (filter == MapFilter.ALL || filter.keyword == null) {
                    places
                } else {
                    places.filter { place ->
                        place.title.contains(filter.keyword) ||
                            place.category.contains(filter.keyword)
                    }
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

        fun updateCurrentLocation(
            lat: Double,
            lng: Double,
        ) {
            _currentLocation.value = LatLngPoint(lat, lng)
        }

        fun selectFilter(filter: MapFilter) {
            _selectedFilter.value = filter
            _selectedPlace.value = null // 필터 변경 시 선택 카드 닫기
        }

        fun searchPlacesNearBy(
            lat: Double,
            lng: Double,
            regionName: String? = null,
        ) {
            viewModelScope.launch {
                _isLoading.value = true
                mapRepository
                    .searchLawPlaces(lat, lng, regionName)
                    .onSuccess { places ->
                        _lawPlaces.value = places
                        _selectedPlace.value = null
                    }.onFailure {
                        _lawPlaces.value = emptyList()
                    }
                _isLoading.value = false
            }
        }

        fun selectPlace(place: LawPlace) {
            _selectedPlace.value = place
        }

        fun clearSelection() {
            _selectedPlace.value = null
        }
    }
