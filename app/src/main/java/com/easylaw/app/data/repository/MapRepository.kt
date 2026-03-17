package com.easylaw.app.data.repository

import com.easylaw.app.domain.model.LawPlace

interface MapRepository {
    suspend fun searchLawPlaces(
        lat: Double,
        lng: Double,
        regionName: String? = null,
    ): Result<List<LawPlace>>
}
