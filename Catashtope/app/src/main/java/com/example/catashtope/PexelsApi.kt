package com.example.catashtope

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PexelsApi {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.pexels.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: PexelsApiService = retrofit.create(PexelsApiService::class.java)
}
