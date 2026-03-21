package com.ohmyguide.app.data.repository

import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.api.TmapApi
import com.ohmyguide.app.data.api.TmapPedestrianRequest
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.domain.model.RouteCoord
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmapRepository @Inject constructor(
    private val tmapApi: TmapApi,
) {
    suspend fun getWalkingRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): ApiResult<List<RouteCoord>> {
        return try {
            val response = tmapApi.getPedestrianRoute(
                appKey = BuildConfig.TMAP_APP_KEY,
                request = TmapPedestrianRequest(
                    startX = startLng.toString(),
                    startY = startLat.toString(),
                    endX = endLng.toString(),
                    endY = endLat.toString(),
                ),
            )

            val coords = mutableListOf<RouteCoord>()
            response.features?.forEach { feature ->
                val geometry = feature.geometry ?: return@forEach
                when (geometry.type) {
                    "LineString" -> {
                        @Suppress("UNCHECKED_CAST")
                        val points = geometry.coordinates as? List<List<Double>> ?: return@forEach
                        points.forEach { point ->
                            if (point.size >= 2) {
                                coords.add(RouteCoord(lat = point[1], lng = point[0]))
                            }
                        }
                    }
                }
            }

            if (coords.size >= 2) {
                ApiResult.Success(coords)
            } else {
                ApiResult.Error(-1, "No walking route found")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Walking route error")
        }
    }
}
