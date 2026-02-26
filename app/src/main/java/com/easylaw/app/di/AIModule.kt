package com.easylaw.app.di

import com.google.ai.client.generativeai.GenerativeModel
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
            modelName = "gemini-2.5-flash",
//            apiKey = "AIzaSyDAHMokux1Lg5Jx9PwfkzIigI8my9Jvamw"
            apiKey = "AIzaSyDDkAUVkk_EdyhW6S3etuVchfJBiAQn-5U",
        )
}
