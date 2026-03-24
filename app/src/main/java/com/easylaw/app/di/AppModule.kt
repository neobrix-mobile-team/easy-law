package com.easylaw.app.di

import android.util.Log
import com.easylaw.app.BuildConfig
import com.easylaw.app.data.datasource.DeepLApi
import com.easylaw.app.data.datasource.KakaoLocalApi
import com.easylaw.app.data.datasource.LawApiService
import com.easylaw.app.data.repository.DiagnosisRepository
import com.easylaw.app.data.repository.DiagnosisRepositoryImpl
import com.easylaw.app.data.repository.LawRepository
import com.easylaw.app.data.repository.LawRepositoryImpl
import com.easylaw.app.data.repository.MapRepository
import com.easylaw.app.data.repository.MapRepositoryImpl
import com.easylaw.app.data.repository.PrecedentAiRepository
import com.easylaw.app.data.repository.PrecedentRepositoryImpl
import com.easylaw.app.data.repository.TranslationRepository
import com.easylaw.app.data.repository.TranslationRepositoryImpl
import com.easylaw.app.util.PreferenceManager
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
private const val HTTP_TIMEOUT_KAKAO_SECONDS = 15L
private const val HTTP_TIMEOUT_DEEPL_SECONDS = 15L
private const val CONNECTION_POOL_MAX_IDLE = 0
private const val CONNECTION_POOL_KEEP_ALIVE = 1L
private const val HTTP_ERROR_CODE_MIN = 400
private const val HTTP_ERROR_CODE_MAX = 599
private const val LAW_BASE_URL = "https://www.law.go.kr/"
private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"
private const val DEEPL_BASE_URL = "https://api-free.deepl.com/"

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KakaoNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeepLNetwork

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
            message.startsWith("-->") -> {
                Log.d(tag, "┌──────────────────────────── [$tag] ───")
                Log.d(tag, "│ ▶ ${message.removePrefix("--> ")}")
            }

            message.startsWith("--> END") -> Log.d(tag, "├─────────────────────────────────────────────")
            message.startsWith("<--") -> {
                val code = message.substringAfter("<-- ").take(3).toIntOrNull() ?: 0
                val logFn: (String, String) -> Unit =
                    if (code in HTTP_ERROR_CODE_MIN..HTTP_ERROR_CODE_MAX) Log::w else Log::d
                logFn(tag, "│ ◀ ${message.removePrefix("<-- ")}")
            }

            message.startsWith("<-- END") -> Log.d(tag, "└─────────────────────────────────────────────")
            message.startsWith("{") || message.startsWith("[") -> {
                try {
                    prettyGson
                        .toJson(JsonParser.parseString(message))
                        .lines()
                        .forEach { Log.d(tag, "│   $it") }
                } catch (e: Exception) {
                    Log.d(tag, "│   $message")
                }
            }

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
    fun provideLawOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(
                Interceptor { chain ->
                    val req =
                        chain
                            .request()
                            .newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                            .header("Accept", "application/json")
                            .header("Connection", "close")
                            .build()
                    chain.proceed(req)
                },
            ).addInterceptor(buildLoggingInterceptor("HTTP_LAW"))
            .retryOnConnectionFailure(true)
            .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS, ConnectionSpec.CLEARTEXT))
            .connectTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(HTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectionPool(ConnectionPool(CONNECTION_POOL_MAX_IDLE, CONNECTION_POOL_KEEP_ALIVE, TimeUnit.NANOSECONDS))
            .protocols(listOf(Protocol.HTTP_1_1))
            .build()

    // ── Kakao OkHttpClient ───────────────────────────────────
    @Provides
    @Singleton
    @KakaoNetwork
    fun provideKakaoOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(
                Interceptor { chain ->
                    val req =
                        chain
                            .request()
                            .newBuilder()
                            .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}")
                            .build()
                    chain.proceed(req)
                },
            ).addInterceptor(buildLoggingInterceptor("HTTP_KAKAO"))
            .connectTimeout(HTTP_TIMEOUT_KAKAO_SECONDS, TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT_KAKAO_SECONDS, TimeUnit.SECONDS)
            .build()

    // DeepL OkHttpClient
    @Provides
    @Singleton
    @DeepLNetwork
    fun provideDeepLOkHttpClient(): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor(
                Interceptor { chain ->
                    val req =
                        chain
                            .request()
                            .newBuilder()
                            .addHeader("Authorization", "DeepL-Auth-Key ${BuildConfig.DEEPL_API_KEY}")
                            .addHeader("Content-Type", "application/json")
                            .build()
                    chain.proceed(req)
                },
            ).addInterceptor(buildLoggingInterceptor("HTTP_DEEPL"))
            .connectTimeout(HTTP_TIMEOUT_DEEPL_SECONDS, TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT_DEEPL_SECONDS, TimeUnit.SECONDS)
            .build()

    // ── Retrofit 인스턴스 ────────────────────────────────────────
    @Provides
    @Singleton
    fun provideLawApiService(okHttpClient: OkHttpClient): LawApiService =
        Retrofit
            .Builder()
            .baseUrl(LAW_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            .build()
            .create(LawApiService::class.java)

    @Provides
    @Singleton
    fun provideKakaoLocalApi(
        @KakaoNetwork okHttpClient: OkHttpClient,
    ): KakaoLocalApi =
        Retrofit
            .Builder()
            .baseUrl(KAKAO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoLocalApi::class.java)

    @Provides
    @Singleton
    fun provideDeepLApi(
        @DeepLNetwork okHttpClient: OkHttpClient,
    ): DeepLApi =
        Retrofit
            .Builder()
            .baseUrl(DEEPL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepLApi::class.java)

    @Provides
    @Singleton
    fun provideLawRepository(apiService: LawApiService): LawRepository = LawRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideDiagnosisRepository(
        apiService: LawApiService,
        generativeModel: GenerativeModel,
        preferenceManager: PreferenceManager,
    ): DiagnosisRepository = DiagnosisRepositoryImpl(apiService, generativeModel, preferenceManager)

    @Provides
    @Singleton
    fun provideMapRepository(apiService: KakaoLocalApi): MapRepository = MapRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun providePrecedentAiRepository(
        generativeModel: GenerativeModel,
        preferenceManager: PreferenceManager,
    ): PrecedentAiRepository = PrecedentRepositoryImpl(generativeModel, preferenceManager)

    @Provides
    @Singleton
    fun provideTranslationRepository(deepLApi: DeepLApi): TranslationRepository = TranslationRepositoryImpl(deepLApi)
}
