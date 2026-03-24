package com.easylaw.app.viewModel.root

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MainViewState(
    val deepLinkPostId: String? = null,
)

@HiltViewModel
class MainViewModel
    @Inject
    constructor() : ViewModel() {
        private val _mainViewState = MutableStateFlow(MainViewState())
        val mainViewState = _mainViewState.asStateFlow()

        init {
            Log.d("MainViewModel", "MainViewModel 초기화")
        }

        fun updateDeepPostId(deepLinkPostId: String?) {
            _mainViewState.value = _mainViewState.value.copy(deepLinkPostId = deepLinkPostId)
        }

        override fun onCleared() {
            super.onCleared()
            Log.d("MainViewModel", "❌ MainViewModel 파괴 (onCleared)")
        }
    }
