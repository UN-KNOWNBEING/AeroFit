package com.aerofit.india.di

import android.content.Context
import com.aerofit.india.domain.model.aqi.AqiCategory
import com.aerofit.india.domain.model.aqi.AqiSnapshot
import com.aerofit.india.domain.repository.IAqiRepository
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- 1. API DEFINITION ---
interface WaqiApiService {
    @GET("feed/geo:{lat};{lon}/")
    suspend fun getAqiByGeo(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("token") token: String
    ): WaqiResponse
}

// --- 2. DTO MODELS (JSON Parsing) ---
data class WaqiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: WaqiData?
)

data class WaqiData(
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("dominentpol") val dominantPollutant: String?
)

// --- 3. REPOSITORY IMPLEMENTATION ---
class RealAqiRepository(private val api: WaqiApiService, private val apiKey: String) : IAqiRepository {
    override suspend fun getAqiForLocation(lat: Double, lon: Double, tileId: String): Result<AqiSnapshot> {
        return try {
            val response = api.getAqiByGeo(lat, lon, apiKey)

            if (response.status == "ok" && response.data != null) {
                val aqi = response.data.aqi
                val pollutant = response.data.dominantPollutant ?: "Unknown"

                // Map AQI number to our Domain Enum
                val category = when (aqi) {
                    in 0..50 -> AqiCategory.Good
                    in 51..100 -> AqiCategory.Satisfactory // Mapped to Moderate in logic
                    in 101..200 -> AqiCategory.Moderate // Logic treats >100 as risky
                    in 201..300 -> AqiCategory.Poor
                    else -> AqiCategory.Severe
                }

                Result.success(
                    AqiSnapshot(
                        overallAqi = aqi,
                        dominantPollutant = pollutant,
                        category = category,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                Result.failure(Exception("API Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// --- 4. DEPENDENCY INJECTION ---
object AppModule {
    // This is a demo token. For production, get your own free token at https://aqicn.org/data-platform/token/
    private const val WAQI_TOKEN = "e900ec28d5aa8ed454302c3e9981d29c11522552"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.waqi.info/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService by lazy {
        retrofit.create(WaqiApiService::class.java)
    }

    fun provideAqiRepository(context: Context): IAqiRepository {
        return RealAqiRepository(apiService, WAQI_TOKEN)
    }
}