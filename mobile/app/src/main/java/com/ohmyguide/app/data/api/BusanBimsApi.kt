package com.ohmyguide.app.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BusanBimsApi {

    @GET("stopArrByBstopid")
    suspend fun getArrivalByStopId(
        @Query("serviceKey") serviceKey: String,
        @Query("bstopid") bstopId: String,
    ): Response<ResponseBody>
}