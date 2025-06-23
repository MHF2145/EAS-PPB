package com.example.catashtope.repository

import com.example.catashtope.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

data class TouristSpot(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

object TouristSpotRepository {
    private val API_KEY = BuildConfig.GEOAPIFY_API_KEY

    suspend fun fetchTouristSpots(query: String): List<TouristSpot> = withContext(Dispatchers.IO) {
        val url = URL("https://api.geoapify.com/v1/geocode/autocomplete?text=${query}&apiKey=$API_KEY")
        val connection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "application/json")

        return@withContext try {
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                parseTouristSpots(response)
            } else {
                println("❌ ${connection.responseCode} ${connection.responseMessage}")
                emptyList()
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseTouristSpots(jsonString: String): List<TouristSpot> {
        val spots = mutableListOf<TouristSpot>()
        val root = JSONObject(jsonString)
        val features = root.getJSONArray("features")

        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val props = feature.getJSONObject("properties")
            val geometry = feature.getJSONObject("geometry")
            val coords = geometry.getJSONArray("coordinates")

            val name = props.optString("formatted", "Unknown")
            val lon = coords.getDouble(0)
            val lat = coords.getDouble(1)

            spots.add(TouristSpot(name, lat, lon))
        }

        return spots
    }
}
