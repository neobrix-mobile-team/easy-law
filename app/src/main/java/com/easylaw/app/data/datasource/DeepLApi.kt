package com.easylaw.app.data.datasource

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepLApi {
    @POST("v2/translate")
    suspend fun translate(
        @Body request: DeepLRequest,
    ): DeepLResponse
}

data class DeepLRequest(
    @SerializedName("text") val text: List<String>,
    @SerializedName("target_lang") val targetLang: String,
    @SerializedName("source_lang") val sourceLang: String = "KO",
)

data class DeepLResponse(
    @SerializedName("translations") val translations: List<DeepLTranslation>,
)

data class DeepLTranslation(
    @SerializedName("text") val text: String,
    @SerializedName("detected_source_language") val detectedSourceLanguage: String,
)
