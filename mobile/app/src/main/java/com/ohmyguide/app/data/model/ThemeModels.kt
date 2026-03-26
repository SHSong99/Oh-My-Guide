package com.ohmyguide.app.data.model

import com.google.gson.annotations.SerializedName

data class ThemeListResponse(
    @SerializedName("count") val count: Int,
    @SerializedName("themes") val themes: List<ThemeInfoDto>,
)

data class ThemeInfoDto(
    @SerializedName("themeId") val themeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("region") val region: String,
)

data class ThemeDetailResponse(
    @SerializedName("themeId") val themeId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String,
    @SerializedName("region") val region: String,
    @SerializedName("attractionCount") val attractionCount: Int,
    @SerializedName("attractions") val attractions: List<ThemeAttractionDto>,
)

data class ThemeAttractionDto(
    @SerializedName("attractionId") val attractionId: Long,
    @SerializedName("image") val image: String?,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("overviewTts") val overviewTts: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("attractionOrder") val attractionOrder: Int,
)
