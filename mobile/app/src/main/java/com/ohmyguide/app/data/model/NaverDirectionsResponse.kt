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
    val section: List<NaverRouteSection>?,
    val guide: List<NaverRouteGuide>?,
)

data class NaverRouteSummary(
    val distance: Int?,
    val duration: Int?,
)

data class NaverRouteSection(
    val pointIndex: Int,
    val pointCount: Int,
    val distance: Int?,
    val name: String?,
    val congestion: Int?,
    val speed: Int?,
)

data class NaverRouteGuide(
    val pointIndex: Int,
    val type: Int?,
    val instructions: String?,
    val distance: Int?,
    val duration: Int?,
)