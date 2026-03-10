package com.easylaw.app.data.datasource

import com.easylaw.app.data.models.NaverLocalResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverSearchApi {
    @GET("v1/search/local.json")
    suspend fun searchLocal(
        @Query("query") query: String,
        @Query("display") display: Int = 5,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "random",
    ): NaverLocalResponse
}
