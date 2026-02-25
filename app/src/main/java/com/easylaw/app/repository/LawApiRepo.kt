package com.easylaw.app.repository

import com.easylaw.app.dto.LawApiResModel
import com.easylaw.app.service.LawApiServiceImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LawApiRepo
    @Inject
    constructor(
        private val apiService: LawApiServiceImpl,
    ) {
        suspend fun getPrecedents(
            query: String,
            curt: String,
        ): LawApiResModel? {
            val res = apiService.searchPrecedent(query = query, curt = curt)
            return res.body()
        }
    }
