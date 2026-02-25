package com.easylaw.app

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

/**
 * EasyLaw 애플리케이션 클래스
 *
 * Hilt를 초기화하는 진입점입니다.
 */
@HiltAndroidApp
class EasyLawApplication : Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
