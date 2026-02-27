package com.easylaw.app.data.datasource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.easylaw.app.domain.model.Precedent
import retrofit2.HttpException
import java.io.IOException

class PrecedentPagingSource(
    private val apiService: LawApiService,
    private val query: String,
    private val org: String?,
    private val onTotalCountFetched: (Int) -> Unit,
) : PagingSource<Int, Precedent>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Precedent> {
        return try {
            val currentPage = params.key ?: 1
            val displaySize = params.loadSize

            if (query.isBlank()) {
                Log.d("PagingSource_LOG", "검색어가 비어있어 API 호출을 중단합니다.")
                return LoadResult.Page(emptyList(), null, null)
            }

            Log.d("PagingSource_LOG", "국가법령 API 호출 시작 - 검색어: $query, 법원코드: $org, 페이지: $currentPage")

            val response =
                apiService.getPrecedentList(
//                apiKey = BuildConfig.LAW_API_KEY,
                    query = query,
                    org = org,
                    page = currentPage,
                    display = displaySize,
                )

            val totalCount = response.precSearch?.totalCnt?.toIntOrNull() ?: 0
            val apiItems = response.precSearch?.precList ?: emptyList()

            Log.d("PagingSource_LOG", "국가법령 API 호출 성공 (200 OK) - 총 검색 건수: $totalCount, 가져온 아이템 수: ${apiItems.size}")

            if (currentPage == 1) {
                onTotalCountFetched(totalCount)
            }

            val mappedItems =
                apiItems.map {
                    Precedent(
                        id = it.caseId ?: "",
                        title = it.title ?: "",
                        category = it.court ?: "",
                        court = it.court ?: "",
                        date = it.date ?: "",
                        detailLink = it.detailLink ?: "",
                        judgmentType = it.judgmentType ?: "",
                    )
                }

            LoadResult.Page(
                data = mappedItems,
                prevKey = if (currentPage == 1) null else currentPage - 1,
                nextKey = if (apiItems.isEmpty() || apiItems.size < displaySize) null else currentPage + 1,
            )
        } catch (e: IOException) {
            // 생소한 구문 설명: IOException은 주로 기기의 와이파이가 꺼져있거나, 서버가 닫혀있어 아예 연결조차 되지 않았을 때 발생합니다.
            Log.e("PagingSource_LOG", "네트워크 오류 발생 (인터넷 연결 확인 필요): ${e.message}", e)
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // 생소한 구문 설명: HttpException은 서버에는 연결되었으나, 서버가 에러 코드(401 인증 실패, 404 경로 없음, 500 서버 에러 등)를 반환했을 때 발생합니다.
            Log.e("PagingSource_LOG", "API 서버 에러 발생 - HTTP 상태 코드: ${e.code()}, 메시지: ${e.message()}", e)
            LoadResult.Error(e)
        } catch (e: Exception) {
            Log.e("PagingSource_LOG", "데이터 파싱 에러 등 알 수 없는 에러 발생: ${e.message}", e)
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Precedent>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
