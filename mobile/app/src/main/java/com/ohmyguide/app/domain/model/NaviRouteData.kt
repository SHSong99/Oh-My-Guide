package com.ohmyguide.app.domain.model

import androidx.compose.ui.graphics.Color

data class RouteCoord(val lat: Double, val lng: Double)

data class StopInfo(
    val name: String,
    val lat: Double,
    val lng: Double,
)

data class RouteSegmentGeo(
    val type: String,
    val coords: List<RouteCoord>,
    val color: Color,
    val lineName: String,
    val fromName: String,
    val toName: String,
    val fromNameKr: String = "",
    val toNameKr: String = "",
    val stopsCount: Int = 0,
    val stops: List<StopInfo> = emptyList(),
)

data class NaviRouteData(
    val mode: String,
    val segments: List<RouteSegmentGeo>,
    val totalDurationMin: Int,
)
