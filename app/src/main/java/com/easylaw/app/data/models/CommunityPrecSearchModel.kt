package com.easylaw.app.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CommunityPrecSearchModel(
    @SerializedName("PrecSearch")
    val precSearch: CommunityLawModel,
)
