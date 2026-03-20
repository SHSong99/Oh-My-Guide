package com.ohmyguide.app.data.model

data class NaverDirectionsResponse(
    val code: Int?,
    val message: String?,
    val route: NaverRouteResult?,
)

data class NaverRouteResult(
    val traoptimal: List<NaverRouteLeg>?,
)

data class NaverRouteLeg(
    val summary: NaverRouteSummary?,
    val path: List<List<Double>>?,
)

data class NaverRouteSummary(
    val distance: Int?,
    val duration: Int?,
)