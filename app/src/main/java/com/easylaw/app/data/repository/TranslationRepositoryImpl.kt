package com.easylaw.app.data.repository

import android.util.Log
import com.easylaw.app.data.datasource.DeepLApi
import com.easylaw.app.data.datasource.DeepLRequest
import javax.inject.Inject

class TranslationRepositoryImpl
    @Inject
    constructor(
        private val deepLApi: DeepLApi,
    ) : TranslationRepository {
        override suspend fun translateTitles(
            texts: List<String>,
            targetLang: String,
        ): Map<String, String> {
            if (texts.isEmpty()) return emptyMap()

            return try {
                Log.d("TranslationRepository", "DeepL 번역 요청 - ${texts.size}건, 언어: $targetLang")
                val response =
                    deepLApi.translate(
                        DeepLRequest(text = texts, targetLang = targetLang),
                    )
                // 원문 리스트와 번역 결과 리스트를 zip으로 매핑
                texts
                    .zip(response.translations.map { it.text })
                    .toMap()
                    .also { Log.d("TranslationRepository", "DeepL 번역 완료 - ${it.size}건") }
            } catch (e: Exception) {
                Log.e("TranslationRepository", "DeepL 번역 실패: ${e.message}")
                // 실패 시 원문 그대로 반환 (번역 실패가 앱 동작을 막으면 안 됨)
                texts.associateWith { it }
            }
        }
    }
