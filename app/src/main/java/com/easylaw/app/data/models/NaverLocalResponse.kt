package com.easylaw.app.data.models

import com.google.gson.annotations.SerializedName

data class NaverLocalResponse(
    @SerializedName("items") val items: List<NaverLocalItem>,
)

data class NaverLocalItem(
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("address") val address: String,
    @SerializedName("roadAddress") val roadAddress: String,
    @SerializedName("telephone") val telephone: String,
    @SerializedName("mapx") val mapx: String,
    @SerializedName("mapy") val mapy: String,
)
