package com.easylaw.app.di

import com.easylaw.app.data.datasource.LawApiService
import com.easylaw.app.data.datasource.PrecedentService
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.data.repository.LawRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt 의존성 주입 모듈
 *
 * 앱 전역에서 사용할 의존성을 정의합니다.
 * 향후 Repository, Service 등을 여기에 추가합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor =
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

        return OkHttpClient
            .Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideLawApiService(okHttpClient: OkHttpClient): LawApiService =
        Retrofit
            .Builder()
            .baseUrl("https://www.law.go.kr/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LawApiService::class.java)

    @Provides
    @Singleton
    fun provideLawRepository(apiService: LawApiService): LawRepository = LawRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideGeminiService(): PrecedentService = PrecedentService()
}
