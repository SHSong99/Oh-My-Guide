package com.ohmyguide.app.data.model

import com.google.gson.annotations.SerializedName

data class AttractionDetailDto(
    @SerializedName("attr_id") val attrId: Long,
    @SerializedName("content_id") val contentId: Int?,
    val title: String?,
    val addr1: String?,
    val tel: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("first_image1") val firstImage1: String?,
    @SerializedName("first_image2") val firstImage2: String?,
    val overview: String?,
    val homepage: String?,
    @SerializedName("content_type_id") val contentTypeId: Long?,
)

data class PhraseBookmarkDto(
    val phraseId: Long,
    val content: String,
    val language: String,
)

data class PickRecommendResponse(
    val placeId: Long,
    val visitCount: Long?,
    val totalScore: Long?,
    val placeRank: Int?,
)
