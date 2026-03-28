package com.ohmyguide.app.data.model

import com.google.gson.annotations.SerializedName

data class GuideNavigationResponse(
    val startLocation: StartLocationDto,
    val destination: GuidePlaceDto,
    val nearbyPlaces: List<GuidePlaceDto> = emptyList(),
)

data class StartLocationDto(
    val latitude: Double,
    val longitude: Double,
)

data class GuidePlaceDto(
    @SerializedName("placeId") val placeId: Long,
    val title: String?,
    val addr1: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("firstImage1") val firstImage1: String?,
    val overview: String?,
    val overviewTts: String?,
)
