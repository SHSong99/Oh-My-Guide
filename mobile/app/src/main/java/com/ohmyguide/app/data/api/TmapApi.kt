package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.model.TmapPedestrianResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TmapApi {
    @POST("tmap/routes/pedestrian?version=1")
    suspend fun getPedestrianRoute(
        @Header("appKey") appKey: String,
        @Body request: TmapPedestrianRequest,
    ): TmapPedestrianResponse
}

data class TmapPedestrianRequest(
    val startX: String,
    val startY: String,
    val endX: String,
    val endY: String,
    val startName: String = "Start",
    val endName: String = "Destination",
    val reqCoordType: String = "WGS84GEO",
    val resCoordType: String = "WGS84GEO",
)
