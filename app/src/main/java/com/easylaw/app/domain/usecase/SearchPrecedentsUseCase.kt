package com.easylaw.app.domain.usecase

import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.data.repository.PrecedentAiRepository
import com.easylaw.app.data.repository.PrecedentResult
import javax.inject.Inject

class SearchPrecedentsUseCase
    @Inject
    constructor(
        private val lawRepository: LawRepository,
        private val aiRepository: PrecedentAiRepository,
        private val keywordOptimizer: com.easylaw.app.util.KeywordOptimizer,
    ) {
        data class Params(
            val situation: String,
            val details: String,
            val orgCode: String?,
            val page: Int = 1,
            val display: Int = 100,
        )

        data class KeywordResolution(
            val keyword: String,
            val wasOptimizedByAi: Boolean,
        )

        /**
         * Gemini 우회 가능 여부를 판단하여 최적 키워드를 반환합니다.
         * ViewModel에서 키워드를 UI에 표시할 때 사용합니다.
         */
        suspend fun resolveKeyword(
            situation: String,
            details: String,
        ): KeywordResolution =
            if (keywordOptimizer.shouldBypassGemini(situation, details)) {
                KeywordResolution(keyword = situation.trim(), wasOptimizedByAi = false)
            } else {
                val keyword = aiRepository.extractKeyword(situation, details)
                KeywordResolution(keyword = keyword, wasOptimizedByAi = true)
            }

        /**
         * 키워드로 판례를 조회합니다.
         */
        suspend operator fun invoke(params: Params): Result<PrecedentResult> =
            lawRepository.getPrecedents(
                query = params.situation,
                org = params.orgCode,
                page = params.page,
                display = params.display,
            )
    }
