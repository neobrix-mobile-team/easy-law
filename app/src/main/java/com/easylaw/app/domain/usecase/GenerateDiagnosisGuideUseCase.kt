package com.easylaw.app.domain.usecase

import com.easylaw.app.data.repository.DiagnosisRepository
import javax.inject.Inject

class GenerateDiagnosisGuideUseCase
    @Inject
    constructor(
        private val repository: DiagnosisRepository,
    ) {
        /**
         * @param context 전체 대화 맥락 (최초 상황 + 추가 Q&A)
         * @return 사용자에게 보여줄 최종 법률 가이드 텍스트
         * @throws Exception Repository 호출 실패 시 — ViewModel의 catch로 전파
         */
        suspend operator fun invoke(context: String): String {
            val lawNames = repository.extractTargetLaws(context)
            val lawDetails = repository.fetchDiagnosisDetails(lawNames)
            return repository.generateFinalGuide(context, lawDetails)
        }
    }
