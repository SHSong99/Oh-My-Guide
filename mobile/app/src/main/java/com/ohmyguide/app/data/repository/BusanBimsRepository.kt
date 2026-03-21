package com.ohmyguide.app.data.repository

import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.api.BusanBimsApi
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.model.BusArrivalInfo
import com.ohmyguide.app.util.BusanBimsXmlParser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusanBimsRepository @Inject constructor(
    private val busanBimsApi: BusanBimsApi,
) {
    suspend fun getArrivalByStopId(bstopId: String): ApiResult<List<BusArrivalInfo>> {
        return try {
            val response = busanBimsApi.getArrivalByStopId(
                serviceKey = BuildConfig.BUSAN_BIMS_SERVICE_KEY,
                bstopId = bstopId,
            )
            val body = response.body()?.string()
            if (response.isSuccessful && body != null) {
                val items = BusanBimsXmlParser.parse(body)
                ApiResult.Success(items)
            } else {
                ApiResult.Error(response.code(), "BIMS API error: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Unknown error")
        }
    }
}