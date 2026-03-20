package com.ohmyguide.app.data.model

data class TmapPedestrianResponse(
    val type: String?,
    val features: List<TmapFeature>?,
)

data class TmapFeature(
    val type: String?,
    val geometry: TmapGeometry?,
    val properties: TmapProperties?,
)

data class TmapGeometry(
    val type: String?,
    val coordinates: Any?,
)

data class TmapProperties(
    val totalDistance: Int?,
    val totalTime: Int?,
    val index: Int?,
    val pointIndex: Int?,
    val name: String?,
    val description: String?,
    val direction: String?,
    val nearPoiName: String?,
    val nearPoiX: String?,
    val nearPoiY: String?,
    val intersectionName: String?,
    val facilityType: String?,
    val facilityName: String?,
    val turnType: Int?,
    val pointType: String?,
    val lineIndex: Int?,
    val distance: Int?,
    val time: Int?,
    val roadType: Int?,
    val categoryRoadType: Int?,
)
