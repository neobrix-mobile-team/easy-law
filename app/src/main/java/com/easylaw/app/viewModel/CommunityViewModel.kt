package com.easylaw.app.viewModel

import android.os.Build
import android.text.Html
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.CategoryModel
import com.easylaw.app.data.models.CommunityNewsModel
import com.easylaw.app.data.models.CommunityPrecModel
import com.easylaw.app.data.models.CommunityWriteModel
import com.easylaw.app.data.models.NaverNewsModel
import com.easylaw.app.data.repository.CommunityRepo
import com.easylaw.app.data.repository.NaverNewsRepo
import com.easylaw.app.domain.model.TopCommenter
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityViewState(
    val isCommunityListLoading: Boolean = false,
    val communityList: List<CommunityWriteModel> = emptyList(),
    val categoryList: Map<String, String> = emptyMap(),
    val selectedCategory: String = "ALL",
    val keywords: List<String> = emptyList(),
    val aiErrorText: String = "",
    val showBottomSheet: Boolean = false,
    val selectedKeyword: String = "",
    val naverNewsItem: NaverNewsModel =
        NaverNewsModel(
            display = 0,
            items = emptyList(),
            lastBuildDate = "",
            start = 0,
            total = 0,
        ),
    val showCounselor: Boolean = false,
    val topCommenters: List<TopCommenter> = emptyList(),
    val communityNewList: List<CommunityNewsModel> = emptyList(),
    val communityPrecList: List<CommunityPrecModel> = emptyList(),
)

// 커뮤니티 화면 뷰 모델
@HiltViewModel
class CommunityViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
        private val ai: GenerativeModel,
        private val naverNewsRepo: NaverNewsRepo,
        private val communityRepo: CommunityRepo,
    ) : ViewModel() {
        private val _communityState = MutableStateFlow(CommunityViewState())
        val communityState = _communityState.asStateFlow()

        init {
            viewModelScope.launch {
                loadCategories()
//                loadCommunityLists()
//                aiCommunity()
            }
        }

        fun refreshCommunityList() {
            viewModelScope.launch {
                loadCommunityLists()
            }
        }

        fun onBottomSheet(keyword: String) {
            _communityState.update {
                it.copy(
                    selectedKeyword = keyword,
                )
            }

            val formatKeyword = "$keyword 변호사 칼럼"

            viewModelScope.launch {
                // api 호출
                fetchNaverNews(formatKeyword)
            }
        }

        suspend fun fetchNaverNews(query: String) {
            try {
                _communityState.update {
                    it.copy(
                        aiErrorText = "",
                        isCommunityListLoading = true,
                        showBottomSheet = false,
                    )
                }

                val res = naverNewsRepo.getNaverNews(query)
//            Log.d("res", res.toString())

                val formatItem =
                    res.items.map { item ->
                        item.copy(
                            description = item.description.decodeHtml(),
                            title = item.title.decodeHtml(),
                            link = item.link.decodeHtml().replace("\\u003d", "="),
                        )
                    }
                Log.d("formatitem", formatItem.toString())
                _communityState.update {
                    it.copy(
                        naverNewsItem = res.copy(items = formatItem),
                        showBottomSheet = true,
                    )
                }
            } catch (e: Exception) {
                Log.e("NAVER FETCH ERROR", e.toString())
                _communityState.update {
                    it.copy(
                        aiErrorText = e.toString(),
                    )
                }
            } finally {
                _communityState.update {
                    it.copy(
                        isCommunityListLoading = false,
                    )
                }
            }
        }

        fun showCounselorList() {
            viewModelScope.launch {
                _communityState.update {
                    it.copy(
                        showCounselor = !it.showCounselor,
                    )
                }
            }
        }

        suspend fun fetchCommunityLaw(newsList: List<CommunityNewsModel>) {
            try {
                // mutableListOf 수정 가능한 리스트
                val tempList = mutableListOf<CommunityPrecModel>()

                newsList.forEach { item ->
                    val res = communityRepo.getCommunityLaw(item.searchQuery)
                    res.precSearch.prec.let {
                        tempList.addAll(it)
                    }
                }

                _communityState.update {
                    it.copy(
                        communityPrecList = tempList,
                    )
                }

//        val res =  communityRepo.getCommunityLaw(query)
                Log.d("커뮤니티 판례", _communityState.value.communityPrecList.toString())
            } catch (e: Exception) {
                Log.e("커뮤니티 판례 에러", e.toString())
            }
        }

        fun closeBottomSheet() {
            _communityState.update {
                it.copy(
                    selectedKeyword = "",
                    showBottomSheet = false,
                )
            }
        }

        fun closeShowDialog() {
            _communityState.update {
                it.copy(
                    aiErrorText = "",
                )
            }
        }

        suspend fun aiCommunity() {
            try {
                val currentList = _communityState.value.communityList
                if (currentList.isEmpty()) {
                    Log.d("aiCommunity", "분석할 게시글이 없습니다.")
                    return
                }

//                val titleList: List<String> = currentList.map { it.title }
//                Log.d("추출제목",titleList.toString())
                // 테스트용 제목
                val sampleTitles: List<String> =
                    listOf(
                        "전세 계약 만료인데 집주인이 보증금을 안 줘요",
                        "묵시적 갱신 후에 중개수수료 제가 내야 하나요?",
                        "월세 2달 밀렸다고 당장 나가라는데 법적으로 맞나요?",
                        "아파트 층간소음 때문에 소송까지 생각 중입니다",
                        "수습기간이라고 최저임금보다 적게 주는데 신고 가능한가요?",
                        "권고사직이랑 해고의 차이가 정확히 뭔가요?",
                        "퇴사 후 한 달이 지났는데 퇴직금이 안 들어옵니다",
                        "직장 내 괴롭힘 증거 수집하는 꿀팁 부탁드려요",
                        "중고거래 사기 당했는데 더치트 신고 후 절차가 궁금해요",
                        "주차장에서 문콕하고 그냥 간 차, 블박으로 잡았습니다",
                        "빌려준 돈 50만 원, 소액심판청구소송 실효성 있을까요?",
                        "에브리타임 익명 게시판 모욕죄 성립 요건 질문",
                        "보이스피싱 인출책인 줄 모르고 가담했는데 처벌 수위는?",
                        "술자리 시비로 폭행 신고 당했는데 합의금 적정선은?",
                        "협의이혼 시 양육비 산정 기준이 어떻게 되나요?",
                        "부모님 빚이 더 많은데 상속포기 vs 한정승인 고민입니다",
                        "사실혼 관계에서도 재산분할 청구가 가능한가요?",
                        "이혼 소송 중 배우자의 외도 증거 확보 방법",
                        "강아지가 행인을 물었는데 견주 책임은 어느 정도인가요?",
                        "층간 누수 피해 보상 범위 어디까지 청구 가능한가요?",
                    )
                val prompt =
                    """
                    당신은 법률 커뮤니티의 분석가이자 검색 최적화 전문가입니다.
                    
                    [데이터]
                    커뮤니티 인기글 제목들: $sampleTitles
                    
                    [작업]
                    위 제목들을 분석하여, 사용자들이 현재 겪고 있는 법률적 고충을 해결할 수 있는 '네이버 뉴스 검색 키워드'를 3개 생성해 주세요.
                    
                    [출력 규칙]
                    1. 각 키워드는 해당 분야의 구체적인 문제 해결(예: 예방법, 판례, 대응책)을 포함해야 합니다.
                    2. 분야명이 아닌, 실제 뉴스 검색창에 넣었을 때 유용한 '검색어' 형태로 출력하세요.
                    3. 군더더기 설명 없이 딱 '검색어'만 콤마(,)로 구분해서 한 줄로 출력하세요.
                    
                    [출력 예시]
                    부동산 전세사기 예방법, 부당해고 구제신청 절차, 층간소음 분쟁 해결 판례
                    """.trimIndent()

                val response = ai.generateContent(prompt)
                val aiResult = response.text ?: "분석 결과를 가져올 수 없습니다."

                val keywords = aiResult.split(",").map { it.trim() }

//                Log.d("Gemini_Simple_Result", keywords.toString())

                _communityState.update {
                    it.copy(
                        keywords = keywords,
                    )
                }
            } catch (e: Exception) {
                Log.e("gemini ai community error", e.toString())
                _communityState.update {
                    it.copy(
                        aiErrorText = e.toString(),
                    )
                }
            }
        }

        suspend fun loadCategories() {
            try {
                val result = supabase.from("categories").select().decodeList<CategoryModel>()

            /*
                associate  :list 형식을 map으로 변환
                ex)
                [
                CategoryModel(key="CIVIL", name="민사"),
                CategoryModel(key="CRIMINAL", name="형사"),
                ]
                    ->
                {
                  "CIVIL" : "민사",
                  "CRIMINAL" : "형사",
                  "LABOR" : "노무"
                }

//                val map = result.associate { it.key to it.name }
//                _communityState.update{
//                    it.copy(
//                        categoryList = map
//                    )
//                }
//                Log.d("카테고리", _communityState.value.categoryList.toString())

             */
                val map = linkedMapOf<String, String>()
                map["ALL"] = "전체"

                result.forEach {
                    map[it.key] = it.name
                }

                _communityState.update {
                    it.copy(categoryList = map)
                }

//                    Log.d("카테고리", "최종 맵: ${_communityState.value.categoryList}")
            } catch (e: Exception) {
                Log.e("Category Error", e.toString())
            }
        }

        suspend fun loadCommunityLists(categoryKey: String = "ALL") {
            _communityState.update { it.copy(isCommunityListLoading = true) }

            try {
                val categoryKoreanName = _communityState.value.categoryList[categoryKey]

                loadTopCommenters()
                loadCommunityNews()
                fetchCommunityLaw(newsList = _communityState.value.communityNewList)

                val result =
                    supabase
                        .from("community")
                        .select(
                            columns =
                                Columns.raw(
                                    """
                                    *,
                                    comment_count:community_comments(count),
                                    comment_like:community_likes(count)
                                    """.trimIndent(),
                                ),
                        ) {
                            filter {
                                if (categoryKey != "ALL" && categoryKoreanName != null) {
                                    eq("category", categoryKoreanName)
                                }
                            }
                            order("created_at", order = Order.DESCENDING)
                        }.decodeList<CommunityWriteModel>()

                _communityState.update { it.copy(communityList = result) }
            } catch (e: Exception) {
                Log.e("supabase community error", e.toString())
            } finally {
                _communityState.update { it.copy(isCommunityListLoading = false) }
            }
        }

        suspend fun loadTopCommenters() {
            try {
                val topCommenters =
                    supabase.postgrest
                        .rpc("get_top_commenters")
                        .decodeList<TopCommenter>()

                _communityState.update { it.copy(topCommenters = topCommenters) }
            } catch (e: Exception) {
                Log.e("TopCommenter Error", e.toString())
            }
        }

        suspend fun loadCommunityNews() {
            try {
                val response =
                    supabase
                        .from("community_news")
                        .select()
                        .decodeList<CommunityNewsModel>()

//            val newsTitles = response.map { it.title }

                _communityState.update {
                    it.copy(
                        communityNewList = response,
                    )
                }

//            Log.d("loadCommunityNews", "가져온 뉴스 개수: ${newsTitles.size}")
            } catch (e: Exception) {
                Log.e("loadCommunityNews error", e.toString())
            }
        }

        fun onCategorySelected(categoryKey: String) {
            viewModelScope.launch {
                _communityState.update {
                    it.copy(selectedCategory = categoryKey)
                }
                loadCommunityLists(categoryKey)
            }
        }

        fun String.decodeHtml(): String {
            if (this.isEmpty()) return ""

            val decoded =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(this).toString()
                }

            return decoded.trim()
        }
    }
