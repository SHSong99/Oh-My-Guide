package com.ohmyguide.app.data.model

import com.google.gson.annotations.SerializedName

data class OdsayResponse(
    val result: OdsayResult?,
)

data class OdsayResult(
    val searchType: Int?,
    val path: List<OdsayPath>?,
)

data class OdsayPath(
    val pathType: Int,
    val info: OdsayPathInfo,
    val subPath: List<OdsaySubPath>,
)

data class OdsayPathInfo(
    val totalTime: Int,
    val totalDistance: Double,
    val payment: Int,
    val busTransitCount: Int,
    val subwayTransitCount: Int,
    val totalWalk: Int,
    val firstStartStation: String?,
    val lastEndStation: String?,
    val totalStationCount: Int?,
    val mapObj: String?,
)

data class OdsaySubPath(
    val trafficType: Int,
    val distance: Double?,
    val sectionTime: Int,
    val stationCount: Int?,
    val startName: String?,
    val endName: String?,
    @SerializedName("startX") val startLng: Double?,
    @SerializedName("startY") val startLat: Double?,
    @SerializedName("endX") val endLng: Double?,
    @SerializedName("endY") val endLat: Double?,
    val lane: List<OdsayLane>?,
    val passStopList: OdsayPassStopList?,
)

data class OdsayLane(
    val name: String?,
    val busNo: String?,
    @SerializedName("type") val busType: Int?,
    val subwayCode: Int?,
)

data class OdsayPassStopList(
    val stations: List<OdsayStation>?,
)

data class OdsayStation(
    @SerializedName("stationName") val name: String?,
    @SerializedName("x") val lng: String?,
    @SerializedName("y") val lat: String?,
    val localStationID: String?,
    val stationCityCode: Int?,
)