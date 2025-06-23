package com.example.catashtope.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class PexelsPhotoResponse(val photos: List<PexelsPhoto>)
data class PexelsPhoto(val id: Int, val photographer: String, val src: PexelsPhotoSrc)
data class PexelsPhotoSrc(val landscape: String)

interface PexelsApiService {
    @GET("v1/search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 1,
        @Query("orientation") orientation: String = "landscape"
    ): Response<PexelsPhotoResponse>
}
