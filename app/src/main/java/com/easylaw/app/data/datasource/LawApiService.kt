package com.easylaw.app.data.datasource

import com.easylaw.app.BuildConfig
import com.easylaw.app.data.models.DiagnosisDetailResponse
import com.easylaw.app.data.models.DiagnosisListResponse
import com.easylaw.app.data.models.LawDetailResponse
import com.easylaw.app.data.models.LawListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface LawApiService {
    // 판례 목록 조회
    @GET("DRF/lawSearch.do?target=prec&type=JSON")
    suspend fun getPrecedentList(
        @Query("OC") apiKey: String = BuildConfig.LAW_API_KEY,
        @Query("query") query: String,
        @Query("org") org: String?,
        @Query("page") page: Int,
        @Query("display") display: Int,
    ): LawListResponse

    // 판례 본문 조회
    @GET("DRF/lawService.do?target=prec&type=JSON")
    suspend fun getPrecedentDetail(
        @Query("OC") apiKey: String = BuildConfig.LAW_API_KEY,
        @Query("ID") caseId: String,
    ): LawDetailResponse

    // 현행 법령 목록 조회
    @GET("DRF/lawSearch.do?target=eflaw&type=JSON")
    suspend fun getStatuteList(
        @Query("OC") apiKey: String = BuildConfig.LAW_API_KEY,
        @Query("query") keyword: String,
    ): DiagnosisListResponse

    // 현행 법령 본문 조회
    @GET("DRF/lawService.do?target=eflaw&type=JSON")
    suspend fun getStatuteDetail(
        @Query("OC") apiKey: String = BuildConfig.LAW_API_KEY,
        @Query("MST") mst: String,
        @Query("ID") lawId: String?,
    ): DiagnosisDetailResponse
}
