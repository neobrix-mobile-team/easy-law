package com.easylaw.app.util

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.easylaw.app.domain.model.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/*
로그인 유저의 세션 관리

1. 앱 실행 중 유저 정보(UserInfo)를 메모리에 유지 (UserState)
2. 로그인 시 정보를 기기(DataStore)에 영구 저장하여 자동 로그인 지원
3. 앱 재시작 시 저장된 데이터를 복구하여 로그인 절차 생략

 */

@Singleton
class PreferenceManager
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private val userDataKey = stringPreferencesKey("user_data")
        private val languageKey = stringPreferencesKey("app_language")

        // Singleton scope: PreferenceManager 생명주기와 동일하게 유지
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        val userData: Flow<UserInfo?> =
            dataStore.data.map { prefs ->
                val json = prefs[userDataKey] ?: return@map null
                try {
                    // 문자열로 저장된 값을 다시 객체화
                    Json.decodeFromString<UserInfo>(json)
                } catch (e: Exception) {
                    Log.e("error", e.toString())
                    null
                }
            }

        // 로그인 시 세션 정보 저장
        suspend fun saveUser(userInfo: UserInfo) {
            val json = Json.encodeToString(userInfo)
            Log.d("PreferenceManager", "DataStore에 저장 시도: $json") // 👈 추가
            dataStore.edit { prefs ->
                prefs[userDataKey] = json
            }
            Log.d("PreferenceManager", "DataStore 저장 완료!") // 👈 추가
        }

        // 로그아웃
        suspend fun sessionClear() {
            dataStore.edit { it.remove(userDataKey) }
        }

        suspend fun saveLanguage(languageCode: String) {
            dataStore.edit { prefs ->
                prefs[languageKey] = languageCode
            }
        }

        val languageState: StateFlow<String> =
            dataStore.data.map { prefs -> prefs[languageKey] ?: "ko" }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = runBlocking { dataStore.data.first()[languageKey] ?: "ko" },
            )
    }
