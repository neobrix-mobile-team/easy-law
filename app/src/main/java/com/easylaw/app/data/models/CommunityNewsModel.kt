package com.easylaw.app.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityNewsModel(
    val id: Int,
    val title: String,
    @SerializedName("search_query")
    val searchQuery: String = "",
)
