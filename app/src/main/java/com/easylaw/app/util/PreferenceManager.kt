package com.easylaw.app.util

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.easylaw.app.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
class PreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val USER_DATA_KEY = stringPreferencesKey("user_data")

    // 기기에 저장된 값 가져오기
    val userData: Flow<UserInfo?> = dataStore.data.map { prefs ->
        val json = prefs[USER_DATA_KEY] ?: return@map null
        try {
            // 문자열로 저장된 값을 다시 객체화
            Json.decodeFromString<UserInfo>(json)
        } catch (e: Exception) {
            Log.e("error",e.toString() )
            null
        }
    }

    // 로그인 시 세션 정보 저장
    suspend fun saveUser(userInfo: UserInfo) {
        val json = Json.encodeToString(userInfo)
        dataStore.edit { prefs ->
            prefs[USER_DATA_KEY] = json
        }
    }
    // 로그아웃
    suspend fun sessionClear() {
        dataStore.edit { it.clear() }
    }
}