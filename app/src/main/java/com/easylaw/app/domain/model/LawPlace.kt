package com.easylaw.app.domain.model

data class LawPlace(
    val title: String,
    val category: String,
    val address: String,
    val roadAddress: String,
    val telephone: String,
    val lat: Double,
    val lng: Double,
)
