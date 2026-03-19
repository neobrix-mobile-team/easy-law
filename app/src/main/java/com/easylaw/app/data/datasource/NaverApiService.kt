package com.easylaw.app.data.datasource

import com.easylaw.app.data.models.NaverNewsModel
import retrofit2.http.GET
import retrofit2.http.Query

interface NaverApiService {
    @GET("v1/search/news.json")
    suspend fun getNaverNews(
        @Query("query") query: String,
        @Query("display") display: Int = 10, // 기본 10개
        @Query("sort") sort: String = "sim", // 유사도순(sim) 또는 날짜순(date)
    ): NaverNewsModel
}
