package com.easylaw.app.common.util

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiUtil {
    const val BASE_URL = "http://www.law.go.kr/"

    @Provides
    @Singleton
    fun provideRetrofit(showErrorInterceptor: ShowErrorInterceptor): Retrofit {
        val logger =
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
                                        java.net.URLDecoder.decode(it, "UTF-8") // 인코딩된 한글을 읽기 쉽게 변환
                                    }

                                android.util.Log.d("OKHTTP_API", "🚀 [METHOD] : $method")
                                android.util.Log.d("OKHTTP_API", "📍 [URL]    : $baseUrl")
                                android.util.Log.d("OKHTTP_API", "📝 [PARAMS] :\n    $params")
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

                    // 3. 응답 결과 라인 (200 OK 등)
                    message.startsWith("<-- 200") || message.startsWith("<-- HTTP") -> {
                        android.util.Log.d("OKHTTP_API", "✅ [RESPONSE STATUS] : $message")
                    }

                    message.startsWith("{") || message.startsWith("[") -> {
                        try {
                            val prettyJson =
                                GsonBuilder().setPrettyPrinting().create().toJson(
                                    JsonParser.parseString(message),
                                )
                            android.util.Log.d("OKHTTP_API", "📦 [BODY] :\n$prettyJson")
                        } catch (e: Exception) {
                            android.util.Log.d("OKHTTP_API", message)
                        }
                    }
                }
            }.apply { level = HttpLoggingInterceptor.Level.BODY }

        val okHttpClient =
            OkHttpClient.Builder()
                .addInterceptor(ErrorLoggingInterceptor())
                .addInterceptor(showErrorInterceptor)
                .addInterceptor(logger) // 가독성이 개선된 로거 적용
                .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

// 인터셉터 설정
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

class ShowErrorInterceptor
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val res = chain.proceed(chain.request())

            showError(res.code)?.let { msg ->
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT)
                        .show()
                }
            }
            return res
        }

        // 에러코드에 따라 추가
        private fun showError(code: Int): String? {
            return when (code) {
                401 -> "인증에 실패했습니다."
                403 -> "접근 권한이 없습니다."
                in 500..599 -> "법령 서버 에러"
                else -> null
            }
        }
    }
