package com.easylaw.app.data.datasource

import com.easylaw.app.data.models.KakaoPlaceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface KakaoLocalApi {
    /**
     * 카카오 키워드 검색 API
     * - 반경 내 법률 관련 시설 키워드별로 검색
     */
    @GET("v2/local/search/keyword.json")
    suspend fun searchByKeyword(
        @Query("query") query: String,
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radiusMeters: Int,
        @Query("sort") sort: String = "distance",
        @Query("size") size: Int = 15,
    ): KakaoPlaceResponse
}
