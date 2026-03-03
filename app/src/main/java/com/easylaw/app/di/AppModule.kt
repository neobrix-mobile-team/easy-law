package com.easylaw.app.di

import android.util.Log
import com.easylaw.app.BuildConfig
import com.easylaw.app.data.datasource.LawApiService
import com.easylaw.app.data.datasource.PrecedentService
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.data.repository.LawRepositoryImpl
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val HTTP_TIMEOUT_SECONDS = 60L
private const val BASE_URL = "https://www.law.go.kr/"

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
        val prettyGson = GsonBuilder().setPrettyPrinting().create()
        val loggingInterceptor =
            HttpLoggingInterceptor { message ->
                // 수정: JSON 형식이 아닐 경우 발생하는 Parsing Error를 방지하기 위해 조건문 강화
                if (message.startsWith("{") || message.startsWith("[")) {
                    try {
                        val jsonElement = JsonParser.parseString(message)
                        val prettyJson = prettyGson.toJson(jsonElement)
                        Log.d("RESTAPI", "╔═══════════════════ JSON BODY ═══════════════════")
                        prettyJson.lines().forEach { Log.d("RESTAPI", "║ $it") }
                        Log.d("RESTAPI", "╚══════════════════════════════════════════════════")
                    } catch (e: Exception) {
                        Log.d("RESTAPI", message)
                    }
                } else {
                    // JSON이 아닌 일반 텍스트(HTML 등)는 그대로 출력하여 에러 원인 파악 유도
                    Log.d("RESTAPI", message)
                }
            }.apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            }

        val headerInterceptor =
            Interceptor { chain ->
                val original = chain.request()
                val request =
                    original
                        .newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        .header("Accept", "application/xml,application/json,*/*")
                        .header("Connection", "keep-alive")
                        .method(original.method, original.body)
                        .build()
                chain.proceed(request)
            }

        return OkHttpClient
            .Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()
    }

    @Provides
    @Singleton
    fun provideLawApiService(okHttpClient: OkHttpClient): LawApiService =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(LawApiService::class.java)

    @Provides
    @Singleton
    fun provideLawRepository(apiService: LawApiService): LawRepository = LawRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideGeminiService(): PrecedentService = PrecedentService()
}
