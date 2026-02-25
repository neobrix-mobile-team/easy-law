package com.easylaw.app.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

data class UserState(
    val userRole: String = "",
    val userInfo: UserInfo? = null
)

@Serializable
data class UserInfo(
    val name: String = "",
    val email: String = "",
)

@Singleton
class UserSession @Inject constructor() {
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    fun setUserRole(role: String) {
        _userState.update { it.copy(userRole = role) }
    }

    fun setLoginInfo(name: String, email: String) {
        _userState.update {
            it.copy(
                userInfo = UserInfo(
                    name = name,
                    email = email,

                )
            )
        }
    }

    fun clear() {
        _userState.value = UserState()
    }
}




