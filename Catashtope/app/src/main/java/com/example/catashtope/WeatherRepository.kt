package com.example.catashtope

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class WeatherRepository(private val context: Context) {

    private val weatherDao = AppDatabase.getDatabase(context).weatherDao()
    private val dataStore = context.dataStore

    companion object {
        val LAST_UPDATED_KEY = longPreferencesKey("last_updated")
    }

    val weatherData: Flow<WeatherResponse?> = weatherDao.getLatestWeather()
    val lastUpdatedTimestamp: Flow<Long?> = dataStore.data.map { it[LAST_UPDATED_KEY] }
    val allWeatherData: Flow<List<WeatherResponse>> = weatherDao.getAllWeatherData()

    suspend fun refreshWeatherData(city: String = "Surabaya") {
        try {
            Log.d("WeatherRepository", "Geocoding city: $city")
            val geo = GeoApi.service.getCoordinates(city)
            val location = geo.results?.firstOrNull()
                ?: throw Exception("Kota tidak ditemukan: $city")

            val lat = location.latitude
            val lon = location.longitude

            Log.d("WeatherRepository", "Fetching weather for $lat,$lon")
            val response = WeatherApi.retrofitService.getWeatherData(lat, lon)
            if (response.isSuccessful && response.body() != null) {
                val newData = response.body()!!.copy(id = 0, cityName = city)
                weatherDao.insertOrUpdate(newData)
                dataStore.edit { it[LAST_UPDATED_KEY] = System.currentTimeMillis() }
                Log.d("WeatherRepository", "Weather data updated for $city")
            } else {
                Log.e("WeatherRepository", "API Failed: ${response.code()} - ${response.errorBody()?.string()}")
                throw Exception("Gagal mengambil data cuaca dari API")
            }
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Exception: ${e.message}")
            throw e
        }
    }
}
