package com.easylaw.app.domain.model

import com.easylaw.app.util.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/*
 * [UserInfo]
 * * 앱 내 인증된 사용자의 핵심 세션 정보를 담는 데이터 모델입니다.
 * 1. Supabase(DB) 유저 테이블과의 데이터 매핑
 * 2. 세션 유지 및 자동 로그인 여부 판단의 기준 ([id] 존재 여부)
 * 3. 사용자 권한(Role)에 따른 맞춤형 UI/UX 제공의 근거
 */

@Serializable
data class UserInfo(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val user_role: String = "",
    val fcm_token: String? = null,
    val created_at: String? = "",
)

@Singleton
class UserSession
    @Inject
    constructor(
        private val preferenceManager: PreferenceManager,
    ) {
        private val _userInfo = MutableStateFlow(UserInfo())
        val userInfo: StateFlow<UserInfo> = _userInfo.asStateFlow()

        // 앱 시작 시 세션 불러오는 동안 다른 화면 보여줄 변수
        private val _isInitialized = MutableStateFlow(false)
        val isInitialized = _isInitialized.asStateFlow()

        fun finishInitialzed() {
            _isInitialized.value = true
        }

        fun setuser_role(role: String) {
            _userInfo.update {
                it.copy(
                    user_role = role,
                )
            }
        }

        fun getuser_role(): String = _userInfo.value.user_role

        suspend fun setLoginInfo(userInfo: UserInfo) {
            _userInfo.update { userInfo }
            _isInitialized.value = true
            preferenceManager.saveUser(userInfo)
        }

        fun getUserState(): UserInfo = _userInfo.value

        fun sessionClear() {
            _userInfo.value = UserInfo()
        }
    }
