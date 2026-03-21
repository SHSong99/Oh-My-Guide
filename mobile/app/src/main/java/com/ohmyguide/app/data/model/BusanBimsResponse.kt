package com.ohmyguide.app.data.model

data class BusArrivalInfo(
    val arsno: String,
    val bstopid: String,
    val lineno: String,
    val lineid: String,
    val nodenm: String,
    val min1: Int,
    val station1: Int,
    val lowplate1: Int,
    val carno1: String,
    val min2: Int,
    val station2: Int,
    val lowplate2: Int,
    val carno2: String,
    val bustype: String,
)