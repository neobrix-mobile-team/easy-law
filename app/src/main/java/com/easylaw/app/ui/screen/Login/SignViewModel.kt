package com.easylaw.app.ui.screen.Login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

@Serializable
data class UserRequest(
    val id: String? = null,
    val name: String,
    val email: String,
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

@HiltViewModel
class SignViewModel
    @Inject
    constructor(
        private val supabase: SupabaseClient,
    ) : ViewModel() {
        private val _signViewState = MutableStateFlow(SignViewState())
        val signViewState = _signViewState.asStateFlow()

        fun onNameChanged(name: String) {
            _signViewState.update { it.copy(name = name) }
        }

        fun onEmailChanged(email: String) {
            val isValid =
                android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            _signViewState.update { it.copy(email = email, isEmailError = email.isNotEmpty() && !isValid) }
        }

        fun onPasswordChanged(pwd: String) {
            val isPwdValid = pwd.isNotEmpty() && pwd.length < 7

            _signViewState.update { it.copy(password = pwd, isPasswordError = isPwdValid) }
        }

        fun onPasswordConfirmChanged(pwd: String) {
            val isPwdVaildConfirm = pwd.isNotEmpty() && _signViewState.value.password != pwd

            _signViewState.update { it ->
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
                            )
                        supabase.from("users").insert(userRequest)
                        _signViewState.update { it.copy(isSignSuccess = true) }
                    }
                } catch (e: Exception) {
                    val errorMsg = e.message ?: ""
                    if (errorMsg.contains("already", ignoreCase = true)) {
                        Log.d("sign error", "이미 가입된 이메일입니다.")
                        _signViewState.update { it ->
                            it.copy(
                                isSignError = "이미 가입된 이메일입니다.",
                            )
                        }
                    } else {
                        Log.d("sign error", "회원가입 실패: ${e.localizedMessage}")
                        _signViewState.update { it ->
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
