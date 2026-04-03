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
    object Transport : Screen("transport/{placeId}?courseId={courseId}&spotIndex={spotIndex}") {
        fun createRoute(
            placeId: String,
            courseId: String? = null,
            spotIndex: Int? = null,
        ): String {
            var route = "transport/$placeId"
            if (courseId != null) route += "?courseId=$courseId&spotIndex=${spotIndex ?: 0}"
            return route
        }
    }
    object TransitDetail : Screen("transit_detail/{placeId}?destLat={destLat}&destLng={destLng}&courseId={courseId}") {
        fun createRoute(placeId: String, destLat: Double, destLng: Double, courseId: String? = null) =
            "transit_detail/$placeId?destLat=$destLat&destLng=$destLng&courseId=${courseId ?: ""}"
    }
    object CourseDetail : Screen("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    object CourseNavi : Screen("course_navi/{courseId}/{mode}") {
        fun createRoute(courseId: String, mode: String = "car") = "course_navi/$courseId/$mode"
    }
    object Rating : Screen("rating/{placeId}/{placeName}") {
        fun createRoute(placeId: String, placeName: String) =
            "rating/$placeId/${java.net.URLEncoder.encode(placeName, "UTF-8")}"
    }
    object Navi : Screen("navi/{placeId}/{mode}?courseId={courseId}&spotIndex={spotIndex}") {
        fun createRoute(
            placeId: String,
            mode: String = "walk",
            courseId: String? = null,
            spotIndex: Int? = null,
        ): String {
            var route = "navi/$placeId/$mode"
            if (courseId != null) route += "?courseId=$courseId&spotIndex=${spotIndex ?: 0}"
            return route
        }
    }
}
