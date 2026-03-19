package com.easylaw.app.data.datasource

import com.easylaw.app.data.models.CommunityPrecSearchModel
import retrofit2.http.GET
import retrofit2.http.Query

interface CommunityApiService {
    @GET("DRF/lawSearch.do")
    suspend fun getCommunityLaw(
        @Query("OC") OC: String = "rhrnak2304",
        @Query("target") target: String = "prec",
        @Query("type") type: String = "JSON",
        @Query("search") search: String = "2",
        @Query("query") query: String,
    ): CommunityPrecSearchModel
}
