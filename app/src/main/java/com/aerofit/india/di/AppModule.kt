package com.aerofit.india.di

import android.content.Context
import com.aerofit.india.domain.model.aqi.AqiCategory
import com.aerofit.india.domain.model.aqi.AqiSnapshot
import com.aerofit.india.domain.repository.IAqiRepository
import kotlinx.coroutines.delay
import kotlin.random.Random

object AppModule {
    fun provideAqiRepository(context: Context): IAqiRepository {
        return object : IAqiRepository {
            override suspend fun getAqiForLocation(lat: Double, lon: Double, tileId: String): Result<AqiSnapshot> {
                delay(500) // Simulate network

                val randomAqi = Random.nextInt(50, 300)

                // EXPLICIT LOGIC (Fixes ambiguous errors)
                val category: AqiCategory
                val pollutant: String

                if (randomAqi <= 100) {
                    category = AqiCategory.Good
                    pollutant = "PM2.5"
                } else if (randomAqi <= 200) {
                    category = AqiCategory.Moderate
                    pollutant = "PM10"
                } else {
                    category = AqiCategory.Severe
                    pollutant = "NO2"
                }

                return Result.success(
                    AqiSnapshot(
                        overallAqi = randomAqi,
                        dominantPollutant = pollutant,
                        category = category,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}