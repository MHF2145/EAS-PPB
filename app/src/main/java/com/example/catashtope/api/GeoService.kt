package com.example.catashtope.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class GeocodingResponse(
    val results: List<GeoResult>?
)

data class GeoResult(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String
)

interface GeoService {
    @GET("v1/search")
    suspend fun getCoordinates(@Query("name") city: String): GeocodingResponse
}

object GeoApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: GeoService by lazy {
        retrofit.create(GeoService::class.java)
    }
}
