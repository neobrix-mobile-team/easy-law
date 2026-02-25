package com.easylaw.app.service

import com.easylaw.app.dto.LawApiResModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// 판례 검색 요청
interface LawApiServiceImpl {
    @GET("DRF/lawSearch.do")
    suspend fun searchPrecedent(
        @Query("OC") oc: String = "rhrnak2304",
        @Query("target") target: String = "prec",
        @Query("type") type: String = "JSON",
        @Query("search") search: String = "2",
        @Query("curt") curt: String,
        @Query("query") query: String,
    ): Response<LawApiResModel>
}
