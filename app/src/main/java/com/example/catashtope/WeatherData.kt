package com.example.catashtope

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "weather_data")
data class WeatherResponse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val latitude: Double,
    val longitude: Double,

    // New Field
    val cityName: String = "Unknown",

    @Embedded(prefix = "current_")
    @SerializedName("current")
    val current: CurrentWeather,

    @Embedded(prefix = "units_")
    @SerializedName("current_units")
    val units: CurrentWeatherUnits
)

data class CurrentWeather(
    val time: String,
    val interval: Int,
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("is_day")
    val isDay: Int,
    val rain: Double,
    val snowfall: Double
)

data class CurrentWeatherUnits(
    val time: String,
    val interval: String,
    @SerializedName("temperature_2m")
    val temperature: String,
    @SerializedName("rain")
    val rain: String,
    @SerializedName("snowfall")
    val snowfall: String,
    @SerializedName("is_day")
    val isDay: String
)

// Add for daily forecast
data class DailyForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val daily: DailyForecastData,
    @SerializedName("daily_units")
    val dailyUnits: DailyForecastUnits
)

data class DailyForecastData(
    val time: List<String>,
    @SerializedName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerializedName("precipitation_sum")
    val precipitationSum: List<Double>
)

data class DailyForecastUnits(
    val time: String,
    @SerializedName("temperature_2m_max")
    val temperatureMax: String,
    @SerializedName("temperature_2m_min")
    val temperatureMin: String,
    @SerializedName("precipitation_sum")
    val precipitationSum: String
)