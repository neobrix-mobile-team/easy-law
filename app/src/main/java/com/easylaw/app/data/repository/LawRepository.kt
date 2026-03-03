package com.easylaw.app.data.repository

import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.domain.model.PrecedentDetail

interface LawRepository {
//    fun getPrecedentsStream(
//        query: String,
//        org: String?,
//        page: Int = 1,
//        onTotalCountFetched: (Int) -> Unit,
//    ): Flow<PagingData<Precedent>>

    suspend fun getPrecedents(
        query: String,
        org: String?,
        page: Int = 1,
        display: Int = 100,
    ): Pair<Int, List<Precedent>>

    suspend fun getPrecedentDetail(caseId: String): PrecedentDetail?
}
