package com.easylaw.app.di

import com.easylaw.app.data.remote.LawApiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideLawApiService(retrofit: Retrofit): LawApiServiceImpl = retrofit.create(LawApiServiceImpl::class.java)
}
