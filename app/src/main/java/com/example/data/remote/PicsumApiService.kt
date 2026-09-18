package com.example.data.remote

import com.example.data.model.PicsumImage
import retrofit2.http.GET
import retrofit2.http.Query

interface PicsumApiService {
    @GET("v2/list")
    suspend fun getImages(
        @Query("page") page: Int,
        @Query("limit") limit: Int = 30
    ): List<PicsumImage>
}
