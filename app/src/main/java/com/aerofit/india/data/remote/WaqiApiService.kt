package com.aerofit.india.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WaqiApiService {

    @GET("feed/geo:{lat};{lon}/")
    suspend fun getAqiByGeo(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("token") token: String
    ): WaqiResponse
}
