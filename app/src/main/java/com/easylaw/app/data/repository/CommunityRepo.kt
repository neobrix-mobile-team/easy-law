package com.easylaw.app.data.repository

import com.easylaw.app.data.datasource.CommunityApiService
import com.easylaw.app.data.models.CommunityPrecSearchModel
import javax.inject.Inject

class CommunityRepo
    @Inject
    constructor(
        private val service: CommunityApiService,
    ) {
        suspend fun getCommunityLaw(query: String): CommunityPrecSearchModel = service.getCommunityLaw(query = query)
    }
