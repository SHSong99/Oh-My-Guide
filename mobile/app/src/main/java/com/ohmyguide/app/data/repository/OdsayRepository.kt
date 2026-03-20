package com.ohmyguide.app.data.repository

import com.ohmyguide.app.BuildConfig
import com.ohmyguide.app.data.api.OdsayApi
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.model.OdsayResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OdsayRepository @Inject constructor(
    private val odsayApi: OdsayApi,
) {
    suspend fun searchTransitPath(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
    ): ApiResult<OdsayResponse> {
        return try {
            val response = odsayApi.searchTransitPath(
                apiKey = BuildConfig.ODSAY_API_KEY,
                startLng = startLng,
                startLat = startLat,
                endLng = endLng,
                endLat = endLat,
            )
            if (response.result?.path != null) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(-1, "No transit routes found")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Unknown error")
        }
    }
}