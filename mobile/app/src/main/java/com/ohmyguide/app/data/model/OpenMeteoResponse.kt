package com.ohmyguide.app.data.model

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    val hourly: HourlyData?,
)

data class HourlyData(
    val time: List<String>?,
    @SerializedName("temperature_2m")
    val temperature: List<Double>?,
    @SerializedName("apparent_temperature")
    val apparentTemperature: List<Double>?,
    @SerializedName("weather_code")
    val weatherCode: List<Int>?,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>?,
    @SerializedName("wind_speed_10m")
    val windSpeed: List<Double>?,
    @SerializedName("is_day")
    val isDay: List<Int>?,
)