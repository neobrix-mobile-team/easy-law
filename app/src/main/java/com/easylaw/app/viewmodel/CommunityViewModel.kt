package com.easylaw.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityViewState(
    val posts: List<Post> = emptyList(),
)

// 커뮤니티 화면 뷰 모델
@HiltViewModel
class CommunityViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
    ) : ViewModel() {
        private val _communityState = MutableStateFlow(CommunityViewState())
        val communityState = _communityState.asStateFlow()

        fun loadCommunityPosts() {
            viewModelScope.launch {
                try {
                    val result =
                        supabase.from("posts")
                            .select(columns = Columns.raw("*, comments(*)"))
                            .decodeList<Post>()

                    _communityState.update {
                        it.copy(posts = result)
                    }
                } catch (e: Exception) {
                    Log.e("supabase posts error", e.toString())
                }
            }
        }
    }
