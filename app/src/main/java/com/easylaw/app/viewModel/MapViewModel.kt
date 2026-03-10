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

data class LatLngPoint(
    val lat: Double,
    val lng: Double,
)

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

        // 현재 사용자의 실제 GPS 위치 (길찾기 출발지로 사용)
        private val _currentLocation = MutableStateFlow<LatLngPoint?>(null)
        val currentLocation: StateFlow<LatLngPoint?> = _currentLocation.asStateFlow()

        fun updateCurrentLocation(
            lat: Double,
            lng: Double,
        ) {
            _currentLocation.value = LatLngPoint(lat, lng)
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
