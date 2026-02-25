package com.easylaw.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt 의존성 주입 모듈
 *
 * 앱 전역에서 사용할 의존성을 정의합니다.
 * 향후 Repository, Service 등을 여기에 추가합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // TODO: 의존성 주입 설정 추가
}
