package com.easylaw.app.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/*
    Retrofit을 사용해 통신을 위한 인터페이스 객체 생성

    @Module, @InstallIn
    공통 객체 선언, 앱이 종료될 떄까지 살아있는다
    @Provides
    외부 라이브러리 객체 생성

 */

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideLawApiService(retrofit: Retrofit): LawApiServiceImpl {
        return retrofit.create(LawApiServiceImpl::class.java)
    }
}
