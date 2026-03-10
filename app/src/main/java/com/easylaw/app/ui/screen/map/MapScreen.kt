package com.easylaw.app.ui.screen.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easylaw.app.R
import com.easylaw.app.domain.model.LawPlace
import com.easylaw.app.viewModel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.overlay.OverlayImage
import kotlinx.coroutines.launch
import java.util.Locale

private fun getRegionName(
    geocoder: Geocoder,
    lat: Double,
    lng: Double,
): String? =
    try {
        @Suppress("DEPRECATION")
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        addresses?.firstOrNull()?.subLocality ?: addresses?.firstOrNull()?.locality
    } catch (e: Exception) {
        null
    }

@SuppressLint("MissingPermission") // Accompanist가 이미 권한을 체크하므로 IDE 경고를 무시합니다.
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context, Locale.KOREAN) }

    val locationPermissionsState =
        rememberMultiplePermissionsState(
            permissions =
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
        )

    val lawPlaces by viewModel.lawPlaces.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    val cameraPositionState = rememberCameraPositionState()
    var isMapMoved by remember { mutableStateOf(false) }
    var isInitialSearchDone by remember { mutableStateOf(false) }
    val hasPermission = locationPermissionsState.permissions.any { it.status.isGranted }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving && cameraPositionState.position.target != LatLng(0.0, 0.0) && isInitialSearchDone) {
            isMapMoved = true
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { myLocation: Location? ->
                if (myLocation != null && !isInitialSearchDone) {
                    val lat = myLocation.latitude
                    val lng = myLocation.longitude

                    viewModel.updateCurrentLocation(lat, lng)
                    cameraPositionState.move(CameraUpdate.scrollTo(LatLng(lat, lng)))

                    val regionName = getRegionName(geocoder, lat, lng)
                    viewModel.searchPlacesNearBy(lat, lng, regionName)
                    isInitialSearchDone = true
                }
            }
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            val locationSource = rememberFusedLocationSource()

            NaverMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                locationSource = locationSource,
                properties =
                    MapProperties(
                        locationTrackingMode = LocationTrackingMode.NoFollow,
                    ),
                onMapClick = { _, _ -> viewModel.clearSelection() },
            ) {
                lawPlaces.forEach { place ->
                    val iconRes =
                        when {
                            place.title.contains("경찰") || place.category.contains("경찰") -> R.drawable.ic_police
                            place.title.contains("법원") || place.category.contains("법원") -> R.drawable.ic_court
                            place.title.contains("검찰") || place.category.contains("검찰") -> R.drawable.ic_prosecutor
                            else -> R.drawable.ic_law_default
                        }

                    Marker(
                        state = MarkerState(position = LatLng(place.lat, place.lng)),
                        captionText = place.title,
                        icon = OverlayImage.fromResource(iconRes),
                        onClick = {
                            viewModel.selectPlace(place)
                            true
                        },
                    )
                }
            }

            if (isLoading) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "주변 법률기관 검색 중...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray,
                            )
                        }
                    }
                }
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
            ) {
                AnimatedVisibility(
                    visible = isMapMoved && !isLoading,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            val currentTarget = cameraPositionState.position.target
                            val regionName = getRegionName(geocoder, currentTarget.latitude, currentTarget.longitude)
                            viewModel.searchPlacesNearBy(currentTarget.latitude, currentTarget.longitude, regionName)
                            isMapMoved = false
                        },
                        icon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                        text = { Text("이 지역에서 검색") },
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                }

                FloatingActionButton(
                    onClick = {
                        fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                            if (loc != null) {
                                viewModel.updateCurrentLocation(loc.latitude, loc.longitude)
                                coroutineScope.launch {
                                    cameraPositionState.animate(CameraUpdate.scrollTo(LatLng(loc.latitude, loc.longitude)))
                                }
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = if (selectedPlace != null) 200.dp else 32.dp, end = 16.dp),
                    containerColor = Color.White,
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "내 위치", tint = Color.Black)
                }

                AnimatedVisibility(
                    visible = selectedPlace != null,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                ) {
                    selectedPlace?.let { place ->
                        PlaceDetailCard(
                            place = place,
                            onNavigateClick = {
                                val urlBuilder = StringBuilder("nmap://route/public?")
                                currentLocation?.let { loc ->
                                    urlBuilder.append("slat=${loc.lat}&slng=${loc.lng}&sname=현재위치&")
                                }
                                urlBuilder.append("dlat=${place.lat}&dlng=${place.lng}&dname=${Uri.encode(place.title)}&appname=com.easylaw.app")

                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlBuilder.toString()))
                                intent.addCategory(Intent.CATEGORY_BROWSABLE)

                                try {
                                    context.startActivity(intent) // 네이버 지도가 설치되어 있으면 실행
                                } catch (e: Exception) {
                                    // 미설치 시 플레이스토어로 이동
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")))
                                }
                            },
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("서비스를 이용하려면 위치 권한이 필요합니다.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text("권한 요청하기")
                }
            }
        }
    }
}

@Composable
fun PlaceDetailCard(
    place: LawPlace,
    onNavigateClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = place.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = place.category.substringAfterLast(">").trim(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            val distanceText =
                if (place.distanceKm < 1.0) {
                    "${(place.distanceKm * 1000).toInt()}m"
                } else {
                    "${"%.1f".format(place.distanceKm)}km"
                }
            Text(text = "📍 $distanceText", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Text(text = place.roadAddress.ifBlank { place.address }, fontSize = 14.sp, color = Color.Gray)
            Text(text = "📞 ${place.telephone}", fontSize = 14.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onNavigateClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text("이곳으로 길 찾기", fontWeight = FontWeight.Bold)
            }
        }
    }
}
