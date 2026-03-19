package com.easylaw.app.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.data.models.CategoryModel
import com.easylaw.app.data.models.CommunityWriteModel
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.util.Numbers
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CommunityWriteViewState(
//    val categories: List<String> = listOf("민사", "형사", "노무", "가사", "기타"),
//    val selectedCategory: String = "",
    val categoryList: Map<String, String> = emptyMap(),
    val selectedCategory: String = "ALL",
    val communityWriteTitleField: String = "",
    val communityWriteContentField: String = "",
    val selectedImages: List<String> = emptyList(),
    val isShowDialog: Boolean = false,
    val previewImage: String? = "",
    val isWriteLoading: Boolean = false,
    val isWriteErrorLoading: Boolean = false,
)

@HiltViewModel
class CommunityWriteViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
        @ApplicationContext private val context: Context,
    ) : ViewModel() {
        private val _commnuityWriteViewState = MutableStateFlow(CommunityWriteViewState())
        val commnuityWriteViewState = _commnuityWriteViewState.asStateFlow()

        // 글쓰기 성공 감지(뒤로가기 용)
        // channel : 하나의 상태를 알려주기 위함
        private val _isWriteSuccess = Channel<Unit>()
        val isWriteSuccess = _isWriteSuccess.receiveAsFlow()

        init {
//            Log.d("ViewModel_LifeCycle", "CommunityWriteViewModel 생성 (HashCode: ${this.hashCode()})")
            viewModelScope.launch {
                loadCategories()
            }
        }

        override fun onCleared() {
            super.onCleared()
//        Log.d("ViewModel_LifeCycle", "CommunityWriteViewModel 파괴 (onCleared)")
        }

        suspend fun loadCategories() {
            try {
                _commnuityWriteViewState.update {
                    it.copy(
                        isWriteLoading = true,
                    )
                }

                val result =
                    supabase
                        .from("categories")
                        .select()
                        .decodeList<CategoryModel>()
                val map = result.associate { it.key to it.name }

                _commnuityWriteViewState.update {
                    it.copy(categoryList = map)
                }
            } catch (e: Exception) {
                Log.e("Category Error", e.toString())
            } finally {
                _commnuityWriteViewState.update {
                    it.copy(
                        isWriteLoading = false,
                    )
                }
            }
        }

        fun onCategorySelected(category: String) {
            _commnuityWriteViewState.update {
                it.copy(selectedCategory = category)
            }
        }

        fun onTitleFieldChanged(title: String) {
            _commnuityWriteViewState.update { it.copy(communityWriteTitleField = title) }
        }

        fun onContentFieldChanged(content: String) {
            _commnuityWriteViewState.update { it.copy(communityWriteContentField = content) }
        }

        // 선택한 이미지 문자열로 저장
        fun onImageAdded(uri: String) {
            _commnuityWriteViewState.update {
                it.copy(selectedImages = it.selectedImages + uri)
            }
        }

        fun removeSelectedImage(uri: String) {
            _commnuityWriteViewState.update {
                it.copy(selectedImages = it.selectedImages - uri)
            }
        }

        fun onShowDialog() {
            _commnuityWriteViewState.update {
                it.copy(isShowDialog = true)
            }
        }

        fun closeShowDialog() {
            _commnuityWriteViewState.update {
                it.copy(isShowDialog = false)
            }
        }

        fun onImagePreview(uri: String) {
            _commnuityWriteViewState.update { it.copy(previewImage = uri) }
        }

        fun onImagePreviewDismissed() {
            _commnuityWriteViewState.update { it.copy(previewImage = "") }
        }

        fun writeCommunity() {
            viewModelScope.launch {
                try {
                    _commnuityWriteViewState.update {
                        it.copy(
                            isWriteLoading = true,
                            isWriteErrorLoading = false,
                        )
                    }

                    // 현재 뷰에서의 상태(변수)
                    val state = _commnuityWriteViewState.value
                    val selectedCategory = state.categoryList[state.selectedCategory] ?: "기타"

                    val uploadedImageUrls =
                        if (state.selectedImages.isNotEmpty()) {
                            uploadImagesToStorage(state.selectedImages)
                        } else {
                            emptyList()
                        }

                    val writeModel =
                        CommunityWriteModel(
                            category = selectedCategory,
                            title = state.communityWriteTitleField,
                            content = state.communityWriteContentField,
                            author = userSession.getUserState().name,
//                            images = state.selectedImages,
                            images = uploadedImageUrls,
                        )
                    supabase.from("community").insert(writeModel)
                    // 성공 신호 보내기
                    _isWriteSuccess.send(Unit)
                } catch (e: Exception) {
                    _commnuityWriteViewState.update {
                        it.copy(
                            isWriteErrorLoading = true,
                        )
                    }
                    Log.e("write error", e.toString())
                } finally {
                    _commnuityWriteViewState.update {
                        it.copy(
                            isWriteLoading = false,
                        )
                    }
                }
            }
        }

        private suspend fun uploadImagesToStorage(uris: List<String>): List<String> {
            val publicUrls = mutableListOf<String>()

            uris.forEach { uriString ->
                val uri = Uri.parse(uriString)
                val fileName = "community_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(Numbers.FIVE)}.jpg"

                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.use { it.readBytes() }

                if (bytes != null) {
                    val bucket = supabase.storage.from("community")

                    bucket.upload(
                        path = fileName,
                        data = bytes,
                        upsert = false,
                    )
                    val url = bucket.publicUrl(fileName)
                    publicUrls.add(url)
                }
            }
            return publicUrls
        }
    }
