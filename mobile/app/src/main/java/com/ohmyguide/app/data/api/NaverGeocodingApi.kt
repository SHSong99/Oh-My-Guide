package com.ohmyguide.app.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverGeocodingApi {
    @GET("gc")
    suspend fun reverseGeocode(
        @Header("X-NCP-APIGW-API-KEY-ID") clientId: String,
        @Header("X-NCP-APIGW-API-KEY") clientSecret: String,
        @Query("coords") coords: String,
        @Query("output") output: String = "json",
        @Query("orders") orders: String = "legalcode",
    ): NaverGeoResponse
}

data class NaverGeoResponse(
    val status: NaverGeoStatus?,
    val results: List<NaverGeoResult>?,
)

data class NaverGeoStatus(
    val code: Int?,
    val name: String?,
)

data class NaverGeoResult(
    val region: NaverGeoRegion?,
)

data class NaverGeoRegion(
    val area1: NaverGeoArea?, // 시/도
    val area2: NaverGeoArea?, // 구/군
    val area3: NaverGeoArea?, // 동
)

data class NaverGeoArea(
    val name: String?,
)
