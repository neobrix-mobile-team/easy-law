package com.easylaw.app.data.models

import com.easylaw.app.domain.model.LawPlace
import com.google.gson.annotations.SerializedName

data class KakaoPlaceResponse(
    @SerializedName("documents") val documents: List<KakaoPlaceDocument>,
)

data class KakaoPlaceDocument(
    @SerializedName("id") val id: String,
    @SerializedName("place_name") val placeName: String,
    @SerializedName("category_name") val categoryName: String,
    @SerializedName("address_name") val addressName: String,
    @SerializedName("road_address_name") val roadAddressName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("x") val x: String, // 경도(longitude)
    @SerializedName("y") val y: String, // 위도(latitude)
    @SerializedName("distance") val distance: String,
) {
    fun toDomain(): LawPlace {
        val lat = y.toDoubleOrNull() ?: 0.0
        val lng = x.toDoubleOrNull() ?: 0.0
        val distanceM = distance.toDoubleOrNull() ?: 0.0

        return LawPlace(
            title = placeName,
            category = categoryName,
            address = addressName,
            roadAddress = roadAddressName,
            telephone = phone.ifBlank { "연락처 정보 없음" },
            lat = lat,
            lng = lng,
            distanceKm = distanceM / 1000.0,
        )
    }
}
