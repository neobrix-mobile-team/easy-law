package com.easylaw.app.domain.usecase

import com.easylaw.app.data.repository.PrecedentAiRepository
import javax.inject.Inject

class SummarizePrecedentUseCase
    @Inject
    constructor(
        private val aiRepository: PrecedentAiRepository,
    ) {
        /**
         * @throws Exception AI 호출 실패 시 — ViewModel의 catch로 전파
         */
        suspend operator fun invoke(
            originalText: String,
            language: String,
            onChunk: (String) -> Unit,
        ): String = aiRepository.summarizePrecedent(originalText, language, onChunk)
    }
