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
