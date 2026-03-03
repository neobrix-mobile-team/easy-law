package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.LawApiService
import com.easylaw.app.domain.model.Precedent
import com.easylaw.app.domain.model.PrecedentDetail
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketException
import javax.inject.Inject

class LawRepositoryImpl
    @Inject
    constructor(
        private val apiService: LawApiService,
    ) : LawRepository {
//        override fun getPrecedentsStream(
//            query: String,
//            org: String?,
//            onTotalCountFetched: (Int) -> Unit,
//        ): Flow<PagingData<Precedent>> =
//            Pager(
//                config =
//                    PagingConfig(
//                        pageSize = 20,
//                        initialLoadSize = 20,
//                        enablePlaceholders = false,
//                        prefetchDistance = 3,
//                    ),
//                pagingSourceFactory = {
//                    PrecedentPagingSource(apiService, query, org, onTotalCountFetched)
//                },
//            ).flow

        override suspend fun getPrecedents(
            query: String,
            org: String?,
            page: Int,
            display: Int,
        ): Pair<Int, List<Precedent>> =
            try {
                Log.d("LawRepositoryImpl", "API 호출 시작 - 검색어: $query, 페이지: $page, 요청개수: $display")

                val response =
                    apiService.getPrecedentList(
                        query = query,
                        org = org,
                        page = page,
                        display = display,
                    )

                val totalCount = response.precSearch?.totalCnt?.toIntOrNull() ?: 0
                val apiItems = response.precSearch?.precList ?: emptyList()

                Log.d("LawRepositoryImpl", "API 호출 완료 - 총 건수: $totalCount, 수신된 건수: ${apiItems.size} (현재 페이지: $page)")

                val mappedItems =
                    apiItems.map {
                        Precedent(
                            id = it.caseId ?: "",
                            title = it.title ?: "",
                            category = it.category ?: "",
                            court = it.court ?: "",
                            date = it.date ?: "",
                            detailLink = it.detailLink ?: "",
                            judgmentType = it.judgmentType ?: "",
                        )
                    }

                Pair(totalCount, mappedItems)
            } catch (e: SocketException) {
                Log.e("LawRepositoryImpl", "서버 연결 끊김(Connection reset) - 페이지 $page: ${e.message}")
                Pair(0, emptyList())
            } catch (e: IOException) {
                Log.e("LawRepositoryImpl", "네트워크/타임아웃 에러 - 페이지 $page: ${e.message}", e)
                Pair(0, emptyList())
            } catch (e: HttpException) {
                Log.e("LawRepositoryImpl", "API 서버 에러 - 상태 코드: ${e.code()}", e)
                Pair(0, emptyList())
            } catch (e: Exception) {
                Log.e("LawRepositoryImpl", "기타 예외 발생 - 페이지 $page: ${e.message}", e)
                Pair(0, emptyList())
            }

        override suspend fun getPrecedentDetail(caseId: String): PrecedentDetail? {
            return try {
                val response = apiService.getPrecedentDetail(caseId = caseId)
                val item = response.precService ?: return null

                val rawText = listOfNotNull(item.issue, item.summary, item.content).joinToString("\n\n")

                PrecedentDetail(
                    caseId = item.caseId ?: caseId,
                    title = item.title ?: "제목 없음",
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
