package com.ohmyguide.app.data.model

import com.google.gson.annotations.SerializedName

data class RefreshRecommendRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("radius_km") val radiusKm: Double = 10.0,
    val category: String? = null,
    val mood: String? = null,
    @SerializedName("free_text") val freeText: String? = null,
    @SerializedName("excluded_attr_ids") val excludedAttrIds: List<Int> = emptyList(),
)

data class PlaceCardDto(
    @SerializedName("attr_id") val attrId: Int,
    val name: String,
    @SerializedName("name_kr") val nameKr: String,
    @SerializedName("image_url") val imageUrl: String?,
    val distance: String,
    val tag: String,
    val latitude: Double,
    val longitude: Double,
)

data class RefreshRecommendResponse(
    val recommendations: List<PlaceCardDto>,
)
