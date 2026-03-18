package com.easylaw.app.common.util

import com.easylaw.app.BuildConfig // BuildConfig 경로 확인 필요
import com.easylaw.app.data.datasource.CommunityApiService
import com.easylaw.app.data.datasource.NaverApiService
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiUtil {
    const val BASE_URL = "https://www.law.go.kr/"
    const val NAVER_BASE_URL = "https://openapi.naver.com/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            when {
                message.startsWith("--> GET") || message.startsWith("--> POST") -> {
                    try {
                        val method = if (message.startsWith("--> GET")) "GET" else "POST"
                        val fullUrl = message.substringAfter("--> $method ").trim()
                        if (fullUrl.contains("?")) {
                            val baseUrl = fullUrl.substringBefore("?")
                            val queryString = fullUrl.substringAfter("?")
                            val params =
                                queryString.split("&").joinToString("\n    ") {
                                    java.net.URLDecoder.decode(it, "UTF-8")
                                }
                            android.util.Log.d("OKHTTP_API", "🚀 [METHOD] : $method\n📍 [URL]    : $baseUrl\n📝 [PARAMS] :\n    $params")
                        } else {
                            android.util.Log.d("OKHTTP_API", message)
                        }
                    } catch (e: Exception) {
                        android.util.Log.d("OKHTTP_API", e.toString())
                    }
                }
                message.contains(":") && !message.startsWith("{") && !message.startsWith("[") -> {
                    android.util.Log.d("OKHTTP_API", "🔑 [HEADER] : $message")
                }
                message.startsWith("<-- 200") || message.startsWith("<-- HTTP") -> {
                    android.util.Log.d("OKHTTP_API", "✅ [RESPONSE STATUS] : $message")
                }
                message.startsWith("{") || message.startsWith("[") -> {
                    try {
                        val prettyJson = GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(message))
                        android.util.Log.d("OKHTTP_API", "📦 [BODY] :\n$prettyJson")
                    } catch (e: Exception) {
                        android.util.Log.d("OKHTTP_API", message)
                    }
                }
            }
        }.apply { level = HttpLoggingInterceptor.Level.BODY }

    // 1. 법제처용 Retrofit (기본)
    @Provides
    @Singleton
    @Named("LawRetrofit") // 🌟 이름 지정
    fun provideLawRetrofit(loggingInterceptor: HttpLoggingInterceptor): Retrofit {
        val okHttpClient =
            OkHttpClient
                .Builder()
                .addInterceptor(ErrorLoggingInterceptor())
                .addInterceptor(loggingInterceptor)
                .build()

        return Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 2. 네이버용 Retrofit (새로 추가)
    @Provides
    @Singleton
    @Named("NaverRetrofit") // 🌟 이름 지정
    fun provideNaverRetrofit(loggingInterceptor: HttpLoggingInterceptor): Retrofit {
        val naverInterceptor =
            Interceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_CLIENT_ID)
                        .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_CLIENT_SECRET)
                        .build()
                chain.proceed(request)
            }

        val okHttpClient =
            OkHttpClient
                .Builder()
                .addInterceptor(naverInterceptor) // 네이버 인증 헤더 추가
                .addInterceptor(ErrorLoggingInterceptor())
                .addInterceptor(loggingInterceptor)
                .build()

        return Retrofit
            .Builder()
            .baseUrl(NAVER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNaverApiService(
        @Named("NaverRetrofit") retrofit: Retrofit,
    ): NaverApiService = retrofit.create(NaverApiService::class.java)

    @Provides
    @Singleton
    fun provideCommunityApiService(
        @Named("LawRetrofit") retrofit: Retrofit,
    ): CommunityApiService = retrofit.create(CommunityApiService::class.java)
}

class ErrorLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        try {
            return chain.proceed(request)
        } catch (e: Exception) {
            android.util.Log.e("API_ERROR", "Request to ${request.url} failed with exception", e)
            throw e
        }
    }
}
