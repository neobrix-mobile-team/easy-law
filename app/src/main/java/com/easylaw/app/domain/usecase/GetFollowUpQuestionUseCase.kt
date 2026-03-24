package com.easylaw.app.domain.usecase

import com.easylaw.app.data.repository.DiagnosisRepository
import com.easylaw.app.domain.model.FollowUpAction
import com.google.ai.client.generativeai.type.Content
import javax.inject.Inject

class GetFollowUpQuestionUseCase
    @Inject
    constructor(
        private val repository: DiagnosisRepository,
    ) {
        companion object {
            const val MAX_QUESTION_COUNT = 3
        }

        /**
         * @param scenario 지금까지의 대화 맥락
         * @param questionCount 현재까지 AI가 질문한 횟수
         * @return 추가 질문이 필요하면 [FollowUpAction.isEnough] = false,
         *         충분하면 [FollowUpAction.isEnough] = true
         */
        suspend operator fun invoke(
            history: List<Content>,
            questionCount: Int,
            language: String,
        ): FollowUpAction {
            // 질문 횟수 한도 도달 시 비즈니스 규칙으로 ENOUGH 처리
            if (questionCount >= MAX_QUESTION_COUNT) {
                return FollowUpAction(isEnough = true)
            }
            return repository.getAdditionalQuestions(history, language)
        }
    }
