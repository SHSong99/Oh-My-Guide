package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.model.NaverDirectionsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverDrivingApi {
    @GET("driving")
    suspend fun getRoute(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("option") option: String = "traoptimal",
    ): NaverDirectionsResponse

    @GET("driving")
    suspend fun getRouteWithWaypoints(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("waypoints") waypoints: String,
        @Query("option") option: String = "traoptimal",
    ): NaverDirectionsResponse
}

interface NaverWalkingApi {
    @GET("driving")
    suspend fun getRoute(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("start") start: String,
        @Query("goal") goal: String,
        @Query("option") option: String = "traoptimal",
    ): NaverDirectionsResponse
}