package com.easylaw.app.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.repository.MapRepository
import com.easylaw.app.domain.model.LawPlace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val mapRepository: MapRepository,
    ) : ViewModel() {
        private val _lawPlaces = MutableStateFlow<List<LawPlace>>(emptyList())
        val lawPlaces: StateFlow<List<LawPlace>> = _lawPlaces.asStateFlow()

        private val _selectedPlace = MutableStateFlow<LawPlace?>(null)
        val selectedPlace: StateFlow<LawPlace?> = _selectedPlace.asStateFlow()

        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        fun searchPlacesNearBy(
            lat: Double,
            lng: Double,
        ) {
            viewModelScope.launch {
                _isLoading.value = true
                mapRepository
                    .searchLawPlaces(lat, lng)
                    .onSuccess { places ->
                        _lawPlaces.value = places
                        _selectedPlace.value = null // 새로운 검색 시 선택 초기화
                    }.onFailure {
                        // 에러 처리 로직 (Snack bar 등)
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
