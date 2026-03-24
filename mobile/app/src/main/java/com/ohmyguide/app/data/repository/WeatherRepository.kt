package com.ohmyguide.app.data.repository

import com.ohmyguide.app.data.api.OpenMeteoApi
import com.ohmyguide.app.data.model.ApiResult
import com.ohmyguide.app.data.model.OpenMeteoResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val openMeteoApi: OpenMeteoApi,
) {
    suspend fun getHourlyForecast(
        latitude: Double,
        longitude: Double,
    ): ApiResult<OpenMeteoResponse> {
        return try {
            val response = openMeteoApi.getHourlyForecast(
                latitude = latitude,
                longitude = longitude,
            )
            if (response.hourly != null) {
                ApiResult.Success(response)
            } else {
                ApiResult.Error(-1, "No weather data")
            }
        } catch (e: Exception) {
            ApiResult.Error(-1, e.message ?: "Weather fetch failed")
        }
    }
}
