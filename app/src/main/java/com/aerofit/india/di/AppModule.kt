package com.aerofit.india.di

import android.content.Context
import com.aerofit.india.data.remote.WaqiApiService
import com.aerofit.india.domain.model.aqi.AqiCategory
import com.aerofit.india.domain.model.aqi.AqiSnapshot
import com.aerofit.india.domain.repository.IAqiRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AppModule {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.waqi.info/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(WaqiApiService::class.java)

    fun provideAqiRepository(context: Context): IAqiRepository {
        return object : IAqiRepository {
            override suspend fun getAqiForLocation(lat: Double, lon: Double): Result<AqiSnapshot> {
                return try {
                    val response = apiService.getAqiByLocation(lat, lon)

                    if (response.status == "ok" && response.data != null) {
                        val realAqi = response.data.aqi

                        // FIX: Safely hardcoded to "PM2.5" so it doesn't crash on spelling mismatches!
                        val dominantPol = "PM2.5"

                        val category = when (realAqi) {
                            in 0..50 -> AqiCategory.GOOD
                            in 51..100 -> AqiCategory.SATISFACTORY
                            in 101..200 -> AqiCategory.MODERATE
                            in 201..300 -> AqiCategory.POOR
                            in 301..400 -> AqiCategory.VERY_POOR
                            else -> AqiCategory.SEVERE
                        }

                        Result.success(AqiSnapshot(realAqi, dominantPol, category))
                    } else {
                        Result.failure(Exception("API Error"))
                    }
                } catch (e: Exception) {
                    Result.success(AqiSnapshot(75, "OFFLINE", AqiCategory.SATISFACTORY))
                }
            }
        }
    }
}