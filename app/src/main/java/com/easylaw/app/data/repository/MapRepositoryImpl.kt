package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.NaverSearchApi
import com.easylaw.app.domain.model.LawPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val RADIUS_KM_PRIMARY = 1.0
private const val RADIUS_KM_FALLBACK = 2.0
private const val MIN_RESULTS_THRESHOLD = 3 // 이 수 미만이면 반경 확장
private const val REQUEST_DELAY_MS = 150L

private const val DISPLAY = 5
private const val PAGE_COUNT = 3 // 5개 × 3페이지 = 최대 15개 수집 시도

private fun haversineDistanceKm(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a =
        sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

class MapRepositoryImpl
    @Inject
    constructor(
        private val naverSearchApi: NaverSearchApi,
    ) : MapRepository {
        override suspend fun searchLawPlaces(
            lat: Double,
            lng: Double,
            regionName: String?,
        ): Result<List<LawPlace>> =
            withContext(Dispatchers.IO) {
                try {
                    // 1차: 반경 1km로 검색
                    val primaryResult = fetchAllPlaces(lat, lng, regionName, RADIUS_KM_PRIMARY)

                    // 결과가 너무 적으면 반경을 2km로 넓혀 재검색
                    val finalPlaces =
                        if (primaryResult.size < MIN_RESULTS_THRESHOLD) {
                            Log.d("MapRepository", "결과 ${primaryResult.size}개 → 반경 ${RADIUS_KM_FALLBACK}km로 확장 재검색")
                            fetchAllPlaces(lat, lng, regionName, RADIUS_KM_FALLBACK)
                        } else {
                            primaryResult
                        }

                    Log.d("MapRepository", "최종 결과: ${finalPlaces.size}개")
                    Result.success(finalPlaces)
                } catch (e: Exception) {
                    Log.e("MapRepository", "전체 검색 프로세스 에러", e)
                    Result.failure(e)
                }
            }

        private suspend fun fetchAllPlaces(
            lat: Double,
            lng: Double,
            regionName: String?,
            radiusKm: Double,
        ): List<LawPlace> {
            val prefix = if (!regionName.isNullOrBlank()) "$regionName " else ""

            data class SearchQuery(
                val query: String,
            )

            val queries =
                listOf(
                    SearchQuery("${prefix}법원"),
                    SearchQuery("${prefix}검찰청"),
                    SearchQuery("${prefix}경찰서"),
                    SearchQuery("${prefix}지구대"),
                    SearchQuery("${prefix}법률구조공단"),
                    SearchQuery("${prefix}등기소"),
                    SearchQuery("${prefix}변호사"),
                    SearchQuery("${prefix}법무사"),
                    SearchQuery("${prefix}법무법인"),
                    SearchQuery("${prefix}법률사무소"),
                )

            val excludeWords =
                listOf(
                    "카페",
                    "음식점",
                    "식당",
                    "커피",
                    "디저트",
                    "편의점",
                    "구내식당",
                    "마트",
                    "미용",
                    "주차장",
                    "테니스",
                )

            val allPlaces = mutableListOf<LawPlace>()
            var requestIndex = 0

            for (searchQuery in queries) {
                // 키워드당 PAGE_COUNT 페이지 페이징
                // display=5 고정이므로 start=1,6,11 로 요청
                for (page in 1..PAGE_COUNT) {
                    val start = (page - 1) * DISPLAY + 1

                    if (requestIndex > 0) delay(REQUEST_DELAY_MS)
                    requestIndex++

                    try {
                        val response =
                            naverSearchApi.searchLocal(
                                query = searchQuery.query,
                                display = DISPLAY,
                                start = start,
                                sort = "random",
                            )

                        val places =
                            response.items.mapNotNull { item ->
                                val title = item.title.replace(Regex("<.*?>"), "")
                                val category = item.category

                                if (excludeWords.any { title.contains(it) || category.contains(it) }) {
                                    return@mapNotNull null
                                }

                                val mapX = item.mapx.toDoubleOrNull()
                                val mapY = item.mapy.toDoubleOrNull()

                                if (mapX != null && mapY != null) {
                                    val finalLng = mapX / 10000000.0
                                    val finalLat = mapY / 10000000.0
                                    val distanceKm = haversineDistanceKm(lat, lng, finalLat, finalLng)

                                    if (distanceKm > radiusKm) return@mapNotNull null

                                    LawPlace(
                                        title = title,
                                        category = category,
                                        address = item.address,
                                        roadAddress = item.roadAddress,
                                        telephone = item.telephone.ifBlank { "연락처 정보 없음" },
                                        lat = finalLat,
                                        lng = finalLng,
                                        distanceKm = distanceKm,
                                    )
                                } else {
                                    null
                                }
                            }

                        allPlaces.addAll(places)

                        // 응답 결과가 DISPLAY보다 적으면 다음 페이지가 없음
                        if (response.items.size < DISPLAY) break
                    } catch (e: Exception) {
                        Log.e("MapRepository", "[${searchQuery.query} p$page] 실패: ${e.message}")
                        break
                    }
                }

                Log.d("MapRepository", "[${searchQuery.query}] 누적 ${allPlaces.size}개 (반경 ${radiusKm}km)")
            }

            return allPlaces
                .distinctBy { it.title + it.address }
                .sortedBy { it.distanceKm }
        }
    }
