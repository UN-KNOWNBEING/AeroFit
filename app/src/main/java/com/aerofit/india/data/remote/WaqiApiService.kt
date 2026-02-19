package com.aerofit.india.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// The API Interface (Using your existing WaqiModels.kt for the response data!)
interface WaqiApiService {
    @GET("feed/geo:{lat};{lon}/")
    suspend fun getAqiByLocation(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("token") token: String = "demo" // We use the free demo token for testing!
    ): WaqiResponse
}