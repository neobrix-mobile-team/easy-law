package com.easylaw.app.ui.screen.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easylaw.app.R
import com.easylaw.app.domain.model.LawPlace
import com.easylaw.app.viewModel.MapFilter
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

private fun markerSizeDpByZoom(zoom: Double): Float =
    when {
        zoom >= 16.0 -> 52f
        zoom >= 14.0 -> 44f
        zoom >= 12.0 -> 36f
        zoom >= 10.0 -> 28f
        else -> 20f
    }

@Composable
fun localizedCategory(category: String): String {
    val raw = category.substringAfterLast(">").trim()
    return when {
        raw.contains("법원") -> stringResource(R.string.facility_court)
        raw.contains("검찰") -> stringResource(R.string.facility_prosecutor)
        raw.contains("경찰") -> stringResource(R.string.facility_police)
        raw.contains("법률구조") -> stringResource(R.string.facility_legal_aid)
        raw.contains("공증") -> stringResource(R.string.facility_notary)
        raw.contains("등기") -> stringResource(R.string.facility_registry)
        raw.contains("법무사") -> stringResource(R.string.facility_judicial_scrivener)
        raw.contains("변호사") -> stringResource(R.string.facility_lawyer)
        raw.contains("법무법인") -> stringResource(R.string.facility_law_firm)
        raw.contains("변리사") -> stringResource(R.string.facility_patent_attorney)
        else -> raw
    }
}

private fun iconResForPlace(place: LawPlace): Int =
    when {
        place.title.contains("법원") || place.category.contains("법원") -> R.drawable.ic_facility_court
        place.title.contains("검찰") || place.category.contains("검찰") -> R.drawable.ic_facility_prosecutor
        place.title.contains("경찰") || place.category.contains("경찰") -> R.drawable.ic_facility_police
        place.title.contains("법률구조") || place.category.contains("법률구조") -> R.drawable.ic_facility_legal_aid
        place.title.contains("공증") -> R.drawable.ic_facility_notary
        place.title.contains("등기") -> R.drawable.ic_facility_registry
        place.title.contains("법무사") -> R.drawable.ic_facility_judicial_scrivener
        else -> R.drawable.ic_facility_law_office
    }

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

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
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

    val filteredPlaces by viewModel.filteredPlaces.collectAsState()
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    val cameraPositionState = rememberCameraPositionState()
    var isMapMoved by remember { mutableStateOf(false) }
    var isInitialSearchDone by remember { mutableStateOf(false) }
    val hasPermission = locationPermissionsState.permissions.any { it.status.isGranted }

    // ── 줌 레벨 실시간 추적 ──────────────────────────────────
    val currentZoom by remember { derivedStateOf { cameraPositionState.position.zoom.toDouble() } }

    // ── 줌·장소 변경 시 클러스터 재계산 ─────────────────────
    val clusters by remember { derivedStateOf { clusterPlaces(filteredPlaces, currentZoom) } }

    // ── 줌에 따른 마커 기본 크기 ─────────────────────────────
    val baseMarkerSizeDp by remember { derivedStateOf { markerSizeDpByZoom(currentZoom) } }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving &&
            cameraPositionState.position.target != LatLng(0.0, 0.0) &&
            isInitialSearchDone
        ) {
            isMapMoved = true
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null && !isInitialSearchDone) {
                    viewModel.updateCurrentLocation(loc.latitude, loc.longitude)
                    cameraPositionState.move(CameraUpdate.scrollTo(LatLng(loc.latitude, loc.longitude)))
                    viewModel.searchPlacesNearBy(loc.latitude, loc.longitude, getRegionName(geocoder, loc.latitude, loc.longitude))
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
                onMapDoubleTab = { _, _ ->
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdate.zoomIn())
                    }
                    true
                },
            ) {
                clusters.forEach { cluster ->
                    if (cluster.isSingle) {
                        // ── 개별 마커 ────────────────────────────────────
                        val place = cluster.representative
                        val isSelected =
                            selectedPlace?.let {
                                it.lat == place.lat && it.lng == place.lng
                            } ?: false

                        // 선택 시 1.5배 확대, 스프링 애니메이션 (통통 튀는 느낌)
                        val sizeScale by animateFloatAsState(
                            targetValue = if (isSelected) 1.5f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.45f, stiffness = 280f),
                            label = "scale_${place.title}",
                        )

                        val finalSize = (baseMarkerSizeDp * sizeScale).dp

                        Marker(
                            state = MarkerState(LatLng(place.lat, place.lng)),
                            captionText = if (isSelected || currentZoom >= 14.0) place.title else "",
                            captionTextSize = if (isSelected) 13.sp else 11.sp,
                            icon = OverlayImage.fromResource(iconResForPlace(place)),
                            width = finalSize,
                            height = finalSize,
                            zIndex = if (isSelected) 10 else 1,
                            onClick = {
                                viewModel.selectPlace(place)
                                true
                            },
                        )
                    } else {
                        // 클러스터 개수에 따라 색상 구분
                        val clusterColor =
                            when {
                                cluster.count >= 10 -> Color(0xFFD32F2F) // 빨강 (많음)
                                cluster.count >= 5 -> Color(0xFFF57C00) // 주황 (중간)
                                else -> Color(0xFF1565C0) // 파랑 (적음)
                            }
                        val clusterSizeDp = (baseMarkerSizeDp * 1.3f).coerceIn(32f, 72f)

                        Marker(
                            state = MarkerState(LatLng(cluster.centerLat, cluster.centerLng)),
                            icon =
                                OverlayImage.fromBitmap(
                                    createClusterBitmap(context, cluster.count, clusterColor, clusterSizeDp),
                                ),
                            width = clusterSizeDp.dp,
                            height = clusterSizeDp.dp,
                            zIndex = 5,
                            onClick = {
                                // 클러스터 클릭 → 해당 영역으로 줌인
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdate.scrollAndZoomTo(
                                            LatLng(cluster.centerLat, cluster.centerLng),
                                            (currentZoom + 2.5).coerceAtMost(18.0),
                                        ),
                                    )
                                }
                                true
                            },
                        )
                    }
                }
            }

            // ── 로딩 오버레이 ─────────────────────────────────────
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
                            Text(stringResource(R.string.map_searching), style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                        }
                    }
                }
            }

            // ── UI 오버레이 버튼 ──────────────────────────────────
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .systemBarsPadding(),
            ) {
                // ── 상단 필터 바 + "이 지역에서 검색" 버튼 ──────────
                Column(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 필터 칩 바
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Spacer(modifier = Modifier.padding(start = 4.dp))
                        MapFilter.entries.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectFilter(filter) },
                                label = { Text(stringResource(filter.labelResId), fontSize = 13.sp) },
                                shape = RoundedCornerShape(20.dp),
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color.White,
                                    ),
                                elevation =
                                    FilterChipDefaults.filterChipElevation(
                                        elevation = 4.dp,
                                        pressedElevation = 2.dp,
                                    ),
                                border = null,
                            )
                        }
                        Spacer(modifier = Modifier.padding(end = 4.dp))
                    }

                    // "이 지역에서 검색" 버튼
                    AnimatedVisibility(
                        visible = isMapMoved && !isLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                val t = cameraPositionState.position.target
                                viewModel.searchPlacesNearBy(t.latitude, t.longitude, getRegionName(geocoder, t.latitude, t.longitude))
                                isMapMoved = false
                            },
                            icon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.map_search_desc)) },
                            text = { Text(stringResource(R.string.map_search_this_area)) },
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    }
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
                    Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.map_my_location_desc), tint = Color.Black)
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
                                val urlBuilder = StringBuilder("nmap://route/car?")
                                currentLocation?.let {
                                    urlBuilder.append("slat=${it.lat}&slng=${it.lng}&sname=현재위치&")
                                }
                                urlBuilder.append("dlat=${place.lat}&dlng=${place.lng}&dname=${Uri.encode(place.title)}&appname=com.easylaw.app")

                                val naverIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlBuilder.toString()))
                                naverIntent.addCategory(Intent.CATEGORY_BROWSABLE)

                                try {
                                    context.startActivity(naverIntent)
                                } catch (e: Exception) {
                                    // 네이버맵 미설치 시 플레이스토어로 이동
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")),
                                    )
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
                Text(stringResource(R.string.map_permission_required))
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                    Text(stringResource(R.string.map_request_permission))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  클러스터 비트맵 생성 (Canvas로 직접 그리기)
//  ComposeView 방식보다 가볍고 안정적
// ─────────────────────────────────────────────────────────────
private fun createClusterBitmap(
    context: android.content.Context,
    count: Int,
    color: Color,
    sizeDp: Float,
): android.graphics.Bitmap {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()
    val bitmap = android.graphics.Bitmap.createBitmap(sizePx, sizePx, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val colorInt =
        android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt(),
        )

    // 외곽 반투명 원
    val outerPaint =
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = colorInt
            alpha = 60
        }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, outerPaint)

    // 내부 원
    val innerPaint =
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = colorInt
        }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.38f, innerPaint)

    // 흰 테두리
    val borderPaint =
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 2.5f * density
        }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.38f, borderPaint)

    // 숫자 텍스트
    val textPaint =
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            this.color = android.graphics.Color.WHITE
            textSize = sizePx * 0.30f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
        }
    val textY = sizePx / 2f - (textPaint.descent() + textPaint.ascent()) / 2
    canvas.drawText("$count", sizePx / 2f, textY, textPaint)

    return bitmap
}

// ─────────────────────────────────────────────────────────────
//  시설 상세 카드
// ─────────────────────────────────────────────────────────────
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
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(place.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = localizedCategory(place.category),
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
            Text("📍 $distanceText", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Text(place.roadAddress.ifBlank { place.address }, fontSize = 14.sp, color = Color.Gray)
            Text("📞 ${place.telephone}", fontSize = 14.sp, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onNavigateClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text(stringResource(R.string.map_navigate), fontWeight = FontWeight.Bold)
            }
        }
    }
}
