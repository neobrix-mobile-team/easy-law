package com.easylaw.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Precedent(
    val id: String,
    val title: String,
    val category: String,
    val court: String,
    val date: String,
    val detailLink: String,
)
