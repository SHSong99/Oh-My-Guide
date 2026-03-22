package com.ohmyguide.app.domain.model

data class RealtimeArrival(
    val busNo: String,
    val stationName: String,
    val min1: Int,
    val station1: Int,
    val min2: Int,
    val station2: Int,
    val busType: String,
)