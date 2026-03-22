package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.model.OdsayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OdsayApi {

    @GET("searchPubTransPathT")
    suspend fun searchTransitPath(
        @Query("apiKey") apiKey: String,
        @Query("SX") startLng: Double,
        @Query("SY") startLat: Double,
        @Query("EX") endLng: Double,
        @Query("EY") endLat: Double,
        @Query("OPT") opt: Int = 0,
        @Query("SearchType") searchType: Int = 0,
        @Query("SearchPathType") searchPathType: Int = 0,
    ): OdsayResponse
}