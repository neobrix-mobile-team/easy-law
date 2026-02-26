package com.easylaw.app.ui.screen.Login

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.common.UserInfo
import com.easylaw.app.common.UserSession
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginViewState(
    val idInput: String = "",
    val pwdInput: String = "",
    val isIdError: Boolean = false,
    val isPwdError: Boolean = false,
    val isLoginLoading: Boolean = false,
    val isLoginError: String = "",
)

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val userSession: UserSession,
        private val supabase: SupabaseClient,
    ) : ViewModel() {
        private val _loginViewState = MutableStateFlow(LoginViewState())
        val loginViewState = _loginViewState.asStateFlow()

        val sessionState = userSession.userState

        fun onChangedIdTextField(id: String) {
            // 이메일 정규식 확인
            val isEmailValid =
                android.util.Patterns.EMAIL_ADDRESS
                    .matcher(id)
                    .matches()
            _loginViewState.update { currentState ->
                currentState.copy(
                    idInput = id,
                    isIdError = id.isNotEmpty() && !isEmailValid,
                )
            }
        }

        fun onChangedPwdTextField(pwd: String) {
            val isPwdValid = pwd.isNotEmpty() && pwd.length < 7

            _loginViewState.update { currentState ->
                currentState.copy(
                    pwdInput = pwd,
                    isPwdError = isPwdValid,
                )
            }
        }

        fun onLoginErrorClose() {
            _loginViewState.update { it.copy(isLoginError = "") }
        }

        fun loadingLogin(goToMainView: () -> Unit) {
            // 보통 비동기 작업을 할 떄는 코루틴 스코프안에서 실행
            viewModelScope.launch {
                _loginViewState.update { it ->
                    it.copy(
                        isLoginLoading = true,
                    )
                }
                try {
                    val email = _loginViewState.value.idInput
                    val password = _loginViewState.value.pwdInput

                    // 입력된 email 로그인 시도
                    supabase.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    val userId = supabase.auth.currentUserOrNull()?.id

                    if (userId != null) {
                        val userInfo =
                            supabase
                                .from("users")
                                .select {
                                    filter {
                                        eq("id", userId)
                                    }
                                }.decodeSingle<UserInfo>()

                        userSession.setLoginInfo(
                            name = userInfo.name, // 인증 메타데이터가 아닌 DB의 name
                            email = userInfo.email, // DB의 email
                        )
                    }

                    Log.d("login user", sessionState.toString())

//                val currentUser = supabase.auth.currentUserOrNull()
//                val name = currentUser?.userMetadata?.get("name")?.toString() ?: "사용자"
//                val userEmail = currentUser?.email ?: email
//
//                userSession.setLoginInfo(
//                    name = name,
//                    email = userEmail
//                )

                    goToMainView()
                } catch (e: Exception) {
                    Log.e("loginError", "로그인 실패: ${e.message}")
                    _loginViewState.update { it.copy(isLoginError = "아이디 또는 비밀번호가 일치하지 않습니다.") }
                } finally {
                    _loginViewState.update { it.copy(isLoginLoading = false) }
                }
            }
        }

        fun signInGoogle(
            context: Context,
            onSuccess: () -> Unit,
        ) {
            val googleAuthClient = GoogleAuthClient(context)
            viewModelScope.launch {
                _loginViewState.update { it.copy(isLoginLoading = true) }

                val googleCredential = googleAuthClient.signIn()

                if (googleCredential != null) {
                    val authCredential =
                        GoogleAuthProvider.getCredential(googleCredential.idToken, null)
                    try {
                        // 1. Firebase 로그인
                        val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                        val user = authResult.user

                        if (user != null) {
                            val userEmail = user.email ?: ""
                            val userName = user.displayName ?: ""
                            val userId = user?.uid

                            val fcmToken =
                                try {
                                    FirebaseMessaging.getInstance().token.await()
                                } catch (e: Exception) {
                                    Log.e("FCM", "토큰 가져오기 실패", e)
                                    null
                                }

                            try {
                                val userData =
                                    mutableMapOf(
                                        "id" to userId,
                                        "email" to userEmail,
                                        "name" to userName,
                                        "fcm_token" to fcmToken,
                                    )

                                supabase.from("users").upsert(
                                    value = userData,
                                    onConflict = "email",
                                )

                                Log.d("Supabase success", "유저 정보 및 FCM 토큰 저장 성공")
                            } catch (e: Exception) {
                                Log.e("Supabase .error", "DB 동기화 에러: ${e.message}")
                            }

                            userSession.setLoginInfo(
                                name = userName,
                                email = userEmail,
                            )

                            onSuccess()
                        }
                    } catch (e: Exception) {
                        Log.d("google auth error", e.toString())
                    } finally {
                        _loginViewState.update { it.copy(isLoginLoading = false) }
                    }
                } else {
                    _loginViewState.update { it.copy(isLoginLoading = false) }
                }
            }
        }

        class GoogleAuthClient(
            private val context: Context,
        ) {
            private val credentialManager = CredentialManager.create(context)

            suspend fun signIn(): GoogleIdTokenCredential? {
                try {
                    // 구글 로그인 옵션 설정
                    val googleIdOption =
                        GetGoogleIdOption
                            .Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("607557323201-jeej7j1udj6iilbn3npbrfeuus71b14g.apps.googleusercontent.com")
                            .build()

                    val request =
                        GetCredentialRequest
                            .Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                    // 팝업 띄우기
                    val result = credentialManager.getCredential(context, request)
                    return GoogleIdTokenCredential.createFrom(result.credential.data)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return null
                }
            }

            suspend fun signOut() {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            }
        }
    }
