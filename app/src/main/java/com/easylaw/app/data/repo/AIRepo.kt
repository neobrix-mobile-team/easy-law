package com.easylaw.app.data.repo

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/*
hilt는 반황형을 보고 메서드를 찾는다.

뷰 : 사용자 클릭
뷰모델 : 중간관리자(데이터 전달)
aiManager : gemini api(객체를 가져옴)
aiModule : gemini 엔진(객체 생성)

 */

data class AlLoading(
    val isLoading: Boolean = false,
    val message: String = "AI가 분석 중입니다...",
)

@Singleton
class AIRepo
    @Inject
    constructor(
        private val generativeModel: GenerativeModel,
    ) {
        private val _loadingState = MutableStateFlow(AlLoading())
        val loadingState = _loadingState.asStateFlow()

        suspend fun execute(prompt: String): String =
            try {
                _loadingState.update { it.copy(isLoading = true) }
                val response = generativeModel.generateContent(prompt)
                response.text?.trim() ?: ""
            } catch (e: Exception) {
                Log.e("error", e.toString())
                "에러가 발생했습니다. 다시 시도해주세요."
            } finally {
                _loadingState.update { it.copy(isLoading = false) }
            }
    }
