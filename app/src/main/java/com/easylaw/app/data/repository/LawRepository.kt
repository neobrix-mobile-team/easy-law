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
    ): Result<PrecedentResult>

    suspend fun getPrecedentDetail(caseId: String): Result<PrecedentDetail>
}

/** getPrecedents 결과를 명시적으로 표현하는 Domain 모델 */
data class PrecedentResult(
    val totalCount: Int,
    val items: List<Precedent>,
)
