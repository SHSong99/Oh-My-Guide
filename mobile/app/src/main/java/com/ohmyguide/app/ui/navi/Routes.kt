package com.ohmyguide.app.ui.navi

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object GpsPermission : Screen("gps_permission")
    object InterestSelect : Screen("interest_select")

    // Main
    object Home : Screen("home?category={category}") {
        fun createRoute(category: String = "") = "home?category=$category"
    }
    object Map : Screen("map")
    object Explore : Screen("explore")
    object Phrases : Screen("phrases")
    object MyPage : Screen("mypage")

    // Onboarding
    object Loading : Screen("loading")

    // Detail
    object Place : Screen("place/{placeId}") {
        fun createRoute(placeId: String) = "place/$placeId"
    }
    object Transport : Screen("transport/{placeId}") {
        fun createRoute(placeId: String) = "transport/$placeId"
    }
    object TransitDetail : Screen("transit_detail/{placeId}") {
        fun createRoute(placeId: String) = "transit_detail/$placeId"
    }
    object CourseDetail : Screen("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    object CourseNavi : Screen("course_navi/{courseId}") {
        fun createRoute(courseId: String) = "course_navi/$courseId"
    }
    object Navi : Screen("navi/{placeId}/{mode}") {
        fun createRoute(placeId: String, mode: String = "walk") = "navi/$placeId/$mode"
    }
}
