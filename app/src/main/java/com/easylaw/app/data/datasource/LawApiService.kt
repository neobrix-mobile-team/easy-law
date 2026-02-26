package com.easylaw.app.data.datasource

import com.easylaw.app.BuildConfig
import com.easylaw.app.data.models.LawDetailResponse
import com.easylaw.app.data.models.LawListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LawApiService {
    @GET("DRF/lawSearch.do?target=prec&type=JSON")
    suspend fun getPrecedentList(
        @Query("OC") apiKey: String = BuildConfig.LAW_API_KEY,
        @Query("query") query: String,
        @Query("org") org: String?,
        @Query("page") page: Int,
        @Query("display") display: Int = 20
    ): LawListResponse

    @GET("DRF/lawService.do?target=prec&type=JSON")
    suspend fun getPrecedentDetail(
        @Query("OC") apiKey: String = BuildConfig.LAW_API_KEY,
        @Query("ID") caseId: String
    ): LawDetailResponse
}