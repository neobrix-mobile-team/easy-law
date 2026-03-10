package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.NaverSearchApi
import com.easylaw.app.domain.model.LawPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MapRepositoryImpl
    @Inject
    constructor(
        private val naverSearchApi: NaverSearchApi,
    ) : MapRepository {
        override suspend fun searchLawPlaces(
            lat: Double,
            lng: Double,
        ): Result<List<LawPlace>> =
            withContext(Dispatchers.IO) {
                try {
                    val keywords = listOf("법원", "법률사무소", "공증", "경찰서", "검찰청", "법률구조공단")

                    val excludeWords = listOf("카페", "음식점", "식당", "커피", "디저트", "편의점", "구내식당", "마트")

                    val deferredResults =
                        keywords.map { query ->
                            async {
                                try {
                                    val response = naverSearchApi.searchLocal(query = query, display = 50)

                                    response.items.mapNotNull { item ->
                                        val title = item.title?.replace(Regex("<.*?>"), "") ?: "이름 없음"
                                        val category = item.category ?: ""

                                        val isExcluded = excludeWords.any { title.contains(it) || category.contains(it) }
                                        if (isExcluded) return@mapNotNull null

                                        val mapX = item.mapx?.toDoubleOrNull()
                                        val mapY = item.mapy?.toDoubleOrNull()

                                        if (mapX != null && mapY != null) {
                                            val finalLng = mapX / 10000000.0
                                            val finalLat = mapY / 10000000.0

                                            LawPlace(
                                                title = title,
                                                category = category,
                                                address = item.address ?: "",
                                                roadAddress = item.roadAddress ?: "",
                                                telephone = item.telephone?.ifBlank { "연락처 정보 없음" } ?: "연락처 정보 없음",
                                                lat = finalLat,
                                                lng = finalLng,
                                            )
                                        } else {
                                            null
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("MapRepository", "[$query] 검색 실패", e) // 실패 원인 출력
                                    emptyList()
                                }
                            }
                        }

                    val mergedPlaces = deferredResults.awaitAll().flatten()
                    val uniquePlaces = mergedPlaces.distinctBy { it.title + it.address }

                    Result.success(uniquePlaces)
                } catch (e: Exception) {
                    Log.e("MapRepository", "전체 검색 프로세스 에러", e)
                    Result.failure(e)
                }
            }
    }
