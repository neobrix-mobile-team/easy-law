package com.easylaw.app.di

import android.util.Log
import com.easylaw.app.BuildConfig
import com.easylaw.app.data.datasource.LawApiService
import com.easylaw.app.data.datasource.NaverSearchApi
import com.easylaw.app.data.datasource.PrecedentService
import com.easylaw.app.data.repository.DiagnosisRepository
import com.easylaw.app.data.repository.DiagnosisRepositoryImpl
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.data.repository.LawRepositoryImpl
import com.easylaw.app.data.repository.MapRepository
import com.easylaw.app.data.repository.MapRepositoryImpl
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

private const val HTTP_TIMEOUT_SECONDS = 60L
private const val BASE_URL = "https://www.law.go.kr/"
private const val NAVER_BASE_URL = "https://openapi.naver.com/"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NaverNetwork

private val prettyGson = GsonBuilder().setPrettyPrinting().create()

/**
 * Hilt 의존성 주입 모듈
 *
 * 앱 전역에서 사용할 의존성을 정의합니다.
 * 향후 Repository, Service 등을 여기에 추가합니다.
 */
private fun buildLoggingInterceptor(tag: String): HttpLoggingInterceptor =
    HttpLoggingInterceptor { message ->
        if (!BuildConfig.DEBUG) return@HttpLoggingInterceptor

        when {
            // ── 요청 첫 줄: "GET https://..." ──────────────────────────
            message.startsWith("-->") -> {
                Log.d(tag, "┌─────────────────────────────────────── [$tag] ───")
                Log.d(tag, "│ ▶ ${message.removePrefix("--> ")}")
            }

            // ── 요청 끝 마커 ────────────────────────────────────────────
            message.startsWith("--> END") -> {
                Log.d(tag, "├───────────────────────────────────────────────────")
            }

            // ── 응답 첫 줄: "<-- 200 OK (243ms)" ───────────────────────
            message.startsWith("<--") -> {
                // 429 같은 에러 응답은 눈에 띄도록 Log.w로 출력
                val code = message.substringAfter("<-- ").take(3).toIntOrNull() ?: 0
                val logFn: (String, String) -> Unit = if (code in 400..599) Log::w else Log::d
                logFn(tag, "│ ◀ ${message.removePrefix("<-- ")}")
            }

            // ── 응답 끝 마커 ────────────────────────────────────────────
            message.startsWith("<-- END") -> {
                Log.d(tag, "└───────────────────────────────────────────────────")
            }

            // ── JSON Body (요청/응답 공통) ───────────────────────────────
            message.startsWith("{") || message.startsWith("[") -> {
                try {
                    val pretty = prettyGson.toJson(JsonParser.parseString(message))
                    pretty.lines().forEach { Log.d(tag, "│   $it") }
                } catch (e: Exception) {
                    Log.d(tag, "│   $message")
                }
            }

            // ── 헤더 / 기타 한 줄 메시지 ────────────────────────────────
            message.isNotBlank() -> Log.d(tag, "│   $message")
        }
    }.apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

// ══════════════════════════════════════════════════════════════
//  Hilt Module
// ══════════════════════════════════════════════════════════════

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val headerInterceptor =
            Interceptor { chain ->
                val original = chain.request()
                val request =
                    original
                        .newBuilder()
                        .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                        .header("Accept", "application/json")
                        .header("Connection", "close")
                        .method(original.method, original.body)
                        .build()
                chain.proceed(request)
            }

        return OkHttpClient
            .Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(buildLoggingInterceptor("HTTP_LAW"))
            .retryOnConnectionFailure(true)
            .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
            .connectTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .readTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()
    }

    // ── 네이버 검색 API OkHttpClient ─────────────────────────────
    @Provides
    @Singleton
    @NaverNetwork
    fun provideNaverOkHttpClient(): OkHttpClient {
        val headerInterceptor =
            Interceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_SEARCH_ID)
                        .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_SEARCH_KEY)
                        .build()
                chain.proceed(request)
            }

        return OkHttpClient
            .Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(buildLoggingInterceptor("HTTP_NAVER"))
            .connectTimeout(15L, TimeUnit.SECONDS)
            .readTimeout(15L, TimeUnit.SECONDS)
            .build()
    }

    // ── Retrofit / Repository 바인딩 ─────────────────────────────
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
    fun provideNaverSearchApi(
        @NaverNetwork okHttpClient: OkHttpClient,
    ): NaverSearchApi =
        Retrofit
            .Builder()
            .baseUrl(NAVER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverSearchApi::class.java)

    @Provides
    @Singleton
    fun provideLawRepository(apiService: LawApiService): LawRepository = LawRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideDiagnosisRepository(
        apiService: LawApiService,
        generativeModel: GenerativeModel,
    ): DiagnosisRepository = DiagnosisRepositoryImpl(apiService, generativeModel)

    @Provides
    @Singleton
    fun provideGeminiService(generativeModel: GenerativeModel): PrecedentService = PrecedentService(generativeModel)

    @Provides
    @Singleton
    fun provideMapRepository(apiService: NaverSearchApi): MapRepository = MapRepositoryImpl(apiService)
}
