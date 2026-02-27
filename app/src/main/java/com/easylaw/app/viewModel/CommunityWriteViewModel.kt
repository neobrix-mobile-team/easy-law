package com.easylaw.app.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CommunityWriteViewState(
    val categories: List<String> = listOf("민사", "형사", "노무", "가사", "기타"),
    val selectedCategory: String = "",
    val communityWriteTitleField: String = "",
    val communityWriteContentField: String = "",
)

@HiltViewModel
class CommunityWriteViewModel
    @Inject
    constructor() : ViewModel() {
        private val _commnuityWriteViewState = MutableStateFlow(CommunityWriteViewState())
        val commnuityWriteViewState = _commnuityWriteViewState.asStateFlow()

        fun onCategorySelected(category: String) {
            _commnuityWriteViewState.update { currentState ->
                val selectedCategory = if (currentState.selectedCategory == category) "" else category
                currentState.copy(selectedCategory = selectedCategory)
            }
//        checkValidation()
        }

        fun onTitleFieldChanged(title: String) {
            _commnuityWriteViewState.update { it.copy(communityWriteTitleField = title) }
        }

        fun onContentFieldChanged(content: String) {
            _commnuityWriteViewState.update { it.copy(communityWriteContentField = content) }
        }
    }
