package com.easylaw.app.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.easylaw.app.data.datasource.LawApiService
import com.easylaw.app.data.datasource.PrecedentPagingSource
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.domain.model.PrecedentDetail
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LawRepositoryImpl
    @Inject
    constructor(
        private val apiService: LawApiService,
    ) : LawRepository {
        override fun getPrecedentsStream(
            query: String,
            org: String?,
            onTotalCountFetched: (Int) -> Unit,
        ): Flow<PagingData<Precedent>> {
            return Pager(
                config =
                    PagingConfig(
                        pageSize = 20,
                        initialLoadSize = 20,
                        enablePlaceholders = false,
                        prefetchDistance = 3,
                    ),
                pagingSourceFactory = {
                    PrecedentPagingSource(apiService, query, org, onTotalCountFetched)
                },
            ).flow
        }

        override suspend fun getPrecedentDetail(caseId: String): PrecedentDetail? {
            return try {
                val response = apiService.getPrecedentDetail(caseId = caseId)
                val item = response.precService ?: return null

                val rawText = listOfNotNull(item.issue, item.summary, item.content).joinToString("\n\n")

                PrecedentDetail(
                    caseId = item.caseId ?: caseId,
                    title = item.title ?: "제목 없음",
                    // 각 항목별로 <br/> 태그를 개행문자로 변경하고 앞뒤 공백을 제거하여 저장
                    issue = item.issue?.replace("<br/>", "\n")?.trim() ?: "",
                    summary = item.summary?.replace("<br/>", "\n")?.trim() ?: "",
                    content = item.content?.replace("<br/>", "\n")?.trim() ?: "",
                )
            } catch (e: Exception) {
                Log.d("getPrecedentDetail Exception -> ", e.message ?: "")
                null
            }
        }
    }
