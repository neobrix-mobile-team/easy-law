package com.easylaw.app.data.repository

import com.easylaw.app.data.datasource.NaverApiService
import com.easylaw.app.data.models.NaverNewsModel
import javax.inject.Inject

class NaverNewsRepo
    @Inject
    constructor(
        private val service: NaverApiService,
    ) {
        suspend fun getNaverNews(query: String): NaverNewsModel = service.getNaverNews(query)
    }
