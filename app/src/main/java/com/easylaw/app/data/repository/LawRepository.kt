package com.easylaw.app.data.repository

import androidx.paging.PagingData
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.domain.model.PrecedentDetail
import kotlinx.coroutines.flow.Flow

interface LawRepository {
    fun getPrecedentsStream(
        query: String,
        org: String?,
        onTotalCountFetched: (Int) -> Unit
    ): Flow<PagingData<Precedent>>

    suspend fun getPrecedentDetail(caseId: String): PrecedentDetail?
}