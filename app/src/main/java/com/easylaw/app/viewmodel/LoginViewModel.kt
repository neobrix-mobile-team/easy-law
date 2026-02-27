package com.easylaw.app.viewmodel

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easylaw.app.domain.model.UserInfo
import com.easylaw.app.domain.model.UserSession
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

/**
 * [LoginViewModel]
 *
 * 앱의 인증(Authentication) 관리를 담당하며, 일반 로그인과 구글 소셜 로그인 로직을 통합 처리합니다.
 *
 * 주요 기능:
 * 1. 실시간 입력 검증: 이메일 형식 및 비밀번호 길이를 실시간으로 체크하여 UI 상태([LoginViewState])를 업데이트합니다.
 * 2. Supabase 이메일 인증: Supabase Auth를 통해 로그인을 수행하고, 성공 시 DB([users] 테이블)에서 유저 상세 정보를 가져와 세션에 저장합니다.
 * 3. 구글 소셜 로그인 (Modern API): 구글 계정 정보를 가져오고, Firebase Auth와 연동하여 인증을 완료합니다.
 * 4. 데이터 동기화 (Upsert): 소셜 로그인 시 유저 정보를 DB에 저장하거나 업데이트하며, 동시에 FCM 토큰을 갱신하여 서버와 동기화합니다.
 * 5. 세션 관리: 인증 성공 시 [UserSession]을 통해 앱 전역에서 사용할 유저 상태 정보를 최신화합니다.
 */

data class LoginViewState(
    val idInput: String = "",
    val pwdInput: String = "",
    val isIdError: Boolean = false,
    val isPwdError: Boolean = false,
    val isLoginLoading: Boolean = false,
    val isLoginError: String = "",
)

private const val MIN_PASSWORD_LENGTH = 8
private const val GOOGLE_CLIENT_ID = "607557323201-jeej7j1udj6iilbn3npbrfeuus71b14g.apps.googleusercontent.com"

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val userSession: UserSession,
        private val supabase: SupabaseClient,
    ) : ViewModel() {
        private val _loginViewState = MutableStateFlow(LoginViewState())
        val loginViewState = _loginViewState.asStateFlow()

        fun onChangedIdTextField(id: String) {
            // 이메일 정규식 확인
            val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(id).matches()
            _loginViewState.update { currentState ->
                currentState.copy(
                    idInput = id,
                    isIdError = id.isNotEmpty() && !isEmailValid,
                )
            }
        }

        fun onChangedPwdTextField(pwd: String) {
            val isPwdValid = pwd.isNotEmpty() && pwd.length < MIN_PASSWORD_LENGTH

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

        fun login(goToMainView: () -> Unit) {
            viewModelScope.launch {
                _loginViewState.update { it.copy(isLoginLoading = true) }

                try {
                    val email = _loginViewState.value.idInput
                    val password = _loginViewState.value.pwdInput

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
                                    filter { eq("id", userId) }
                                }.decodeSingle<UserInfo>()

                        userSession.setLoginInfo(userInfo)
                        Log.d("userInfo", userSession.getUserState().toString())

                        goToMainView()
                    } else {
                        throw Exception("유저 정보를 찾을 수 없습니다.")
                    }
                } catch (e: Exception) {
                    Log.e("loginError", "로그인 실패: ${e.message}")
                    _loginViewState.update {
                        it.copy(isLoginError = "아이디 또는 비밀번호가 일치하지 않습니다.")
                    }
                } finally {
                    _loginViewState.update { it.copy(isLoginLoading = false) }
                }
            }
        }

        fun logInGoogle(
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
                            val userRole = userSession.getUserRole()

                            val fcmToken =
                                try {
                                    FirebaseMessaging.getInstance().token.await()
                                } catch (e: Exception) {
                                    Log.e("FCM", "토큰 가져오기 실패", e)
                                    null
                                }

                            try {
                                val userData =
                                    UserRequest(
                                        id = userId,
                                        name = userName,
                                        email = userEmail,
                                        user_role = userRole,
                                        fcmToken = fcmToken,
                                    )
                                val userInfo =
                                    supabase
                                        .from("users")
                                        .upsert(value = userData, onConflict = "email") {
                                            select()
                                        }.decodeSingle<UserInfo>()

                                userSession.setLoginInfo(userInfo)

//                                supabase.from("users").upsert(
//                                    value = userData,
//                                    onConflict = "email",
//                                )
                                Log.d("userInfo", userSession.getUserState().toString())
                                Log.d("Supabase success", "유저 정보 및 FCM 토큰 저장 성공")
                            } catch (e: Exception) {
                                Log.e("Supabase .error", "DB 동기화 에러: ${e.message}")
                            }

//                            userSession.setLoginInfo(
//                                name = userName,
//                                email = userEmail,
//                            )
//                            sessionState.value.userInfo?.name
//                            val info = sessionState.value
//                            Log.d("login_user", "이름: ${info.userInfo?.name}, 이메일: ${info.userInfo?.email}")

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
                            .setServerClientId(GOOGLE_CLIENT_ID)
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
        }
    }
