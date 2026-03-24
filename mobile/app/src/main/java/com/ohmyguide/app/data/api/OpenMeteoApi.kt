package com.ohmyguide.app.data.api

import com.ohmyguide.app.data.model.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApi {

    @GET("forecast")
    suspend fun getHourlyForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m,apparent_temperature,weather_code,precipitation_probability,wind_speed_10m,is_day",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("wind_speed_unit") windSpeedUnit: String = "ms",
    ): OpenMeteoResponse
}