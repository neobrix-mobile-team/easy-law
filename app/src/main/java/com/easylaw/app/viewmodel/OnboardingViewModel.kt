package com.easylaw.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.easylaw.app.domain.model.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * [OnboardingViewModel]
 *
 * 앱 최초 진입 시 사용자의 특성을 파악하고 서비스 이용 환경을 설정하는 온보딩 프로세스를 관리합니다.
 *
 * 주요 기능:
 * 1. 단계별 흐름 제어: 온보딩의 현재 페이지(Step) 상태를 관리하여 사용자가 순차적으로 정보를 확인하게 합니다.
 * 2. 사용자 역할(Role) 정의: 사용자가 '내국인'인지 '외국인'인지 선택한 정보를 캡처합니다.
 * 3. 데이터 임시 저장: 선택된 역할 정보를 [UserSession]에 즉시 반영하여, 이후 회원가입 시 DB에 올바른 역할이 저장되도록 징검다리 역할을 수행합니다.
 */


data class OnboardingViewState(
    val currentStep: Int = 1,
    val user_role: String = "",
)

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val userSession: UserSession,
    ) : ViewModel() {
        private val _onboardingVState = MutableStateFlow(OnboardingViewState())
        val onboardingViewState = _onboardingVState.asStateFlow()

        // 사용자 유형 선택 (선택 시 상태 업데이트)
        fun selectRole(role: String) {
            _onboardingVState.update { it.copy(user_role = role) }

            val user_role = when(role){
                "내국인" -> "내국인"
                else -> "외국인"
            }

            userSession.setuser_role(
                user_role
            )
        }

        // 다음 단계로 이동
        fun nextStep() {
            val currentState = _onboardingVState.value
            _onboardingVState.update { it.copy(currentStep = currentState.currentStep + 1) }
        }

        fun previousStep() {
            if (_onboardingVState.value.currentStep > 1) {
                _onboardingVState.update { it.copy(currentStep = it.currentStep - 1) }
            }
        }
    }
