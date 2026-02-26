package com.easylaw.app.data.repo

import com.easylaw.app.data.models.LawApiResModel
import com.easylaw.app.data.remote.LawApiServiceImpl
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
