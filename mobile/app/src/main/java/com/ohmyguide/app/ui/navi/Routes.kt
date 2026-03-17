package com.ohmyguide.app.ui.navi

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object GpsPermission : Screen("gps_permission")
    object InterestSelect : Screen("interest_select")

    // Main
    object Home : Screen("home")
    object Map : Screen("map")
    object Explore : Screen("explore")
    object Phrases : Screen("phrases")
    object MyPage : Screen("mypage")

    // Detail
    object Place : Screen("place/{placeId}") {
        fun createRoute(placeId: String) = "place/$placeId"
    }
    object Navi : Screen("navi/{placeId}") {
        fun createRoute(placeId: String) = "navi/$placeId"
    }
    object Story : Screen("story/{placeId}") {
        fun createRoute(placeId: String) = "story/$placeId"
    }
}
