package com.easylaw.app.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.domain.model.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * [SignViewModel]
 *
 * 앱의 회원가입(Sign Up) 프로세스를 관리하는 ViewModel입니다.
 * Supabase Auth를 통한 계정 생성과 서비스 DB(users 테이블)에 사용자 정보를 동기화하는 역할을 수행합니다.
 *
 * 주요 기능:
 * 1. UI 상태 관리: 이름, 이메일, 비밀번호 입력값의 유효성 검사 및 에러 상태 관리
 * 2. 계정 생성: Supabase Auth를 이용한 Email/Password 기반 회원가입 처리.

 */

@Serializable
data class UserRequest(
    val id: String? = null,
    val name: String,
    val email: String,
    val user_role: String,
    @SerialName("fcm_token")
    val fcmToken: String? = null,
)

data class SignViewState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isNameError: Boolean = false,
    val isEmailError: Boolean = false,
    val isPasswordError: Boolean = false,
    val isPasswordConfirmError: Boolean = false,
    val isSignLoading: Boolean = false,
    val isSignSuccess: Boolean = false,
    val isSignError: String = "",
)

private const val MIN_PASSWORD_LENGTH = 8

@HiltViewModel
class SignViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
        private val userSession: UserSession,
    ) : ViewModel() {
        private val _signViewState = MutableStateFlow(SignViewState())
        val signViewState = _signViewState.asStateFlow()

        fun onNameChanged(name: String) {
            _signViewState.update { it.copy(name = name) }
        }

        fun onEmailChanged(email: String) {
            val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
            _signViewState.update { it.copy(email = email, isEmailError = email.isNotEmpty() && !isValid) }
        }

        fun onPasswordChanged(pwd: String) {
            val isPwdValid = pwd.isNotEmpty() && pwd.length < MIN_PASSWORD_LENGTH

            _signViewState.update { it.copy(password = pwd, isPasswordError = isPwdValid) }
        }

        fun onPasswordConfirmChanged(pwd: String) {
            val isPwdVaildConfirm = pwd.isNotEmpty() && _signViewState.value.password != pwd

            _signViewState.update {
                it.copy(
                    passwordConfirm = pwd,
                    isPasswordConfirmError = isPwdVaildConfirm,
                )
            }
        }

        fun onSignErrorClose() {
            _signViewState.update { it.copy(isSignError = "") }
        }

        fun signUp() {
            viewModelScope.launch {
                _signViewState.update { it.copy(isSignLoading = true) }
                try {
                    val email = _signViewState.value.email
                    val password = _signViewState.value.password
                    val name = _signViewState.value.name
                    val userRole = userSession.getuser_role()

                    Log.d("현재 유저상태", userRole)

                    supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    val user = supabase.auth.currentUserOrNull()
                    val userId = user?.id

                    if (userId != null) {
                        val userRequest =
                            UserRequest(
                                id = userId,
                                name = name,
                                email = email,
                                user_role = userRole,
                            )
                        supabase.from("users").insert(userRequest)
                        _signViewState.update { it.copy(isSignSuccess = true) }
                    }
                } catch (e: Exception) {
                    val errorMsg = e.message ?: ""
                    if (errorMsg.contains("already", ignoreCase = true)) {
                        Log.d("sign error", "이미 가입된 이메일입니다.")
                        _signViewState.update {
                            it.copy(
                                isSignError = "이미 가입된 이메일입니다.",
                            )
                        }
                    } else {
                        Log.d("sign error", "회원가입 실패: ${e.localizedMessage}")
                        _signViewState.update {
                            it.copy(
                                isSignError = e.localizedMessage ?: "",
                            )
                        }
                    }
                } finally {
                    _signViewState.update { it.copy(isSignLoading = false) }
                }
            }
        }
    }
