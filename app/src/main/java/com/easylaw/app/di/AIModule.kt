package com.easylaw.app.di

import com.easylaw.app.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel =
        GenerativeModel(
            modelName = "gemini-3.1-flash-lite-preview",
//            modelName = "gemini-2.5-flash-lite",
            apiKey = BuildConfig.GEMINI_API_KEY,
            requestOptions =
                RequestOptions(
                    timeout = 30_000, // 30초 타임아웃 — 초과 시 TimeoutCancellationException 발생
                ),
        )
}
