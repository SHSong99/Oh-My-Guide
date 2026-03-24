package com.ohmyguide.app.data.repository

import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.api.NaverDrivingApi
import com.ohmyguide.app.data.api.NaverWalkingApi
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.model.NaverDirectionsResponse
import com.ohmyguide.app.domain.model.RouteCoord
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NaverDirectionsRepository @Inject constructor(
    private val drivingApi: NaverDrivingApi,
    private val walkingApi: NaverWalkingApi,
) {
    suspend fun getDrivingRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): ApiResult<List<RouteCoord>> {
        return try {
            val response = drivingApi.getRoute(
                clientId = BuildConfig.NAVER_MAP_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_MAP_CLIENT_SECRET,
                start = "$startLng,$startLat",
                goal = "$endLng,$endLat",
            )
            parseResponse(response)
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Driving route error")
        }
    }

    suspend fun getWalkingRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): ApiResult<List<RouteCoord>> {
        return try {
            val response = walkingApi.getRoute(
                clientId = BuildConfig.NAVER_MAP_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_MAP_CLIENT_SECRET,
                start = "$startLng,$startLat",
                goal = "$endLng,$endLat",
            )
            parseResponse(response)
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Walking route error")
        }
    }

    suspend fun getDrivingRouteWithWaypoints(
        startLat: Double,
        startLng: Double,
        waypoints: List<Pair<Double, Double>>,
        endLat: Double,
        endLng: Double,
    ): ApiResult<List<RouteCoord>> {
        return try {
            val waypointsStr = waypoints.joinToString(":") { (lat, lng) -> "$lng,$lat" }
            val response = drivingApi.getRouteWithWaypoints(
                clientId = BuildConfig.NAVER_MAP_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_MAP_CLIENT_SECRET,
                start = "$startLng,$startLat",
                goal = "$endLng,$endLat",
                waypoints = waypointsStr,
            )
            parseResponse(response)
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Waypoint route error")
        }
    }

    fun parseFullResponse(response: NaverDirectionsResponse) = response.route?.traoptimal?.firstOrNull()

    private fun parseResponse(response: NaverDirectionsResponse): ApiResult<List<RouteCoord>> {
        val path = response.route?.traoptimal?.firstOrNull()?.path
        if (path.isNullOrEmpty()) {
            return ApiResult.Error(response.code ?: -1, response.message ?: "No route found")
        }
        val coords = path.map { point ->
            RouteCoord(lat = point[1], lng = point[0])
        }
        return ApiResult.Success(coords)
    }
}