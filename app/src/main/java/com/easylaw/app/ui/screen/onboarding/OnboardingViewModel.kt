package com.easylaw.app.ui.screen.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.easylaw.app.common.UserSession
import javax.inject.Inject

data class  OnboardingViewState(
    val currentStep: Int = 1,
    val userRole: String = "",
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userSession: UserSession
) : ViewModel() {

    private val _onboardingVState = MutableStateFlow(OnboardingViewState())
    val onboardingViewState = _onboardingVState.asStateFlow()

    // 사용자 유형 선택 (선택 시 상태 업데이트)
    fun selectRole(role: String) {

        _onboardingVState.update { it.copy(userRole = role) }
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
    
    // 온보딩 작업 후 데이터 저장, 재시작하면 온보딩 생략 등등
    fun completeOnboarding() {
        userSession.setUserRole(
            _onboardingVState.value.userRole
        )
        Log.d("userRoleComple", _onboardingVState.value.userRole)
    }
}