package com.easylaw.app.data.models

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class LawListResponse(
    @SerializedName("PrecSearch") val precSearch: PrecSearchData?
)

data class PrecSearchData(
    @SerializedName("totalCnt") val totalCnt: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("prec")
    @JsonAdapter(PrecItemListDeserializer::class)
    val precList: List<PrecItem>?
)

data class PrecItem(
    @SerializedName("판례일련번호") val caseId: String?,
    @SerializedName("사건명") val title: String?,
    @SerializedName("사건종류명") val category: String?,
    @SerializedName("법원명") val court: String?,
    @SerializedName("선고일자") val date: String?,
    @SerializedName("판례상세링크") val detailLink: String?
)

// 역직렬화
class PrecItemListDeserializer : JsonDeserializer<List<PrecItem>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): List<PrecItem> {
        return when {
            json.isJsonArray -> {
                json.asJsonArray.map { context.deserialize(it, PrecItem::class.java) }
            }

            json.isJsonObject -> {
                val singleItem = context.deserialize<PrecItem>(json, PrecItem::class.java)
                listOf(singleItem)
            }

            else -> emptyList()
        }
    }
}
