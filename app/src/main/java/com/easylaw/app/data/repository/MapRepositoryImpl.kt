package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.KakaoLocalApi
import com.easylaw.app.domain.model.LawPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val RADIUS_METERS_PRIMARY = 1000
private const val RADIUS_METERS_FALLBACK = 2000
private const val MIN_RESULTS_THRESHOLD = 3

class MapRepositoryImpl
    @Inject
    constructor(
        private val kakaoLocalApi: KakaoLocalApi,
    ) : MapRepository {
        private val legalKeywords =
            listOf(
                "법원",
                "검찰청",
                "법률사무소",
                "법무법인",
                "법률구조공단",
                "공증사무소",
                "경찰서",
                "변호사",
                "법무사",
                "등기소",
                "변리사",
                "특허법인",
            )

        override suspend fun searchLawPlaces(
            lat: Double,
            lng: Double,
            regionName: String?,
        ): Result<List<LawPlace>> =
            withContext(Dispatchers.IO) {
                runCatching {
                    val primaryResult = fetchAllPlaces(lat, lng, RADIUS_METERS_PRIMARY)

                    val finalPlaces =
                        if (primaryResult.size < MIN_RESULTS_THRESHOLD) {
                            Log.d("MapRepository", "결과 ${primaryResult.size}개 → 반경 ${RADIUS_METERS_FALLBACK}m로 확장 재검색")
                            fetchAllPlaces(lat, lng, RADIUS_METERS_FALLBACK)
                        } else {
                            primaryResult
                        }

                    Log.d("MapRepository", "최종 결과: ${finalPlaces.size}개")
                    finalPlaces
                }
            }

        private suspend fun fetchAllPlaces(
            lat: Double,
            lng: Double,
            radiusMeters: Int,
        ): List<LawPlace> =
            legalKeywords
                .flatMap { keyword ->
                    try {
                        kakaoLocalApi
                            .searchByKeyword(
                                query = keyword,
                                longitude = lng,
                                latitude = lat,
                                radiusMeters = radiusMeters,
                            ).documents
                    } catch (e: Exception) {
                        Log.e("MapRepository", "[$keyword] 검색 실패: ${e.message}")
                        emptyList()
                    }
                }.distinctBy { it.id }
                .map { it.toDomain() }
                .sortedBy { it.distanceKm }
    }
