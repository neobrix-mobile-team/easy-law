package com.easylaw.app.domain.usecase

import com.easylaw.app.data.repository.DiagnosisRepository
import com.google.ai.client.generativeai.type.Content
import javax.inject.Inject

class GenerateDiagnosisGuideUseCase
    @Inject
    constructor(
        private val repository: DiagnosisRepository,
    ) {
        /**
         * @param history 전체 대화 맥락 (최초 상황 + 추가 Q&A)
         * @param language 진단 시작 시점에 캡처된 언어 — 전체 흐름에 고정
         * @return 사용자에게 보여줄 최종 법률 가이드 텍스트
         * @throws Exception Repository 호출 실패 시 — ViewModel의 catch로 전파
         */
        suspend operator fun invoke(
            history: List<Content>,
            language: String,
            onChunk: (String) -> Unit,
        ): String {
            val lawNames = repository.extractTargetLaws(history)
            val lawDetails = repository.fetchDiagnosisDetails(lawNames)
            return repository.generateFinalGuide(history, lawDetails, language, onChunk)
        }
    }
