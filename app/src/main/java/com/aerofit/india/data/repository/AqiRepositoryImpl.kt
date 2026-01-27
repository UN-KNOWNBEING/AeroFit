package com.aerofit.india.data.repository

import com.aerofit.india.data.local.AqiDao
import com.aerofit.india.data.local.AqiEntity
import com.aerofit.india.data.remote.WaqiApiService
import com.aerofit.india.domain.model.aqi.*
import com.aerofit.india.domain.repository.IAqiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class AqiRepositoryImpl(
    private val apiService: WaqiApiService,
    private val aqiDao: AqiDao,
    private val apiKey: String
) : IAqiRepository {

    private val FRESH_TIMEOUT = 60 * 60 * 1000L
    private val STALE_TIMEOUT = 24 * 60 * 60 * 1000L

    override fun observeAqiForCell(cellId: String): Flow<AqiSnapshot> = flow {
        val now = System.currentTimeMillis()
        val cached = aqiDao.getAqiForTile(cellId)

        if (cached != null && now - cached.lastUpdatedTimestamp < FRESH_TIMEOUT) {
            emit(mapEntityToDomain(cached))
        }

        try {
            val response = apiService.getAqiByGeo(
                lat = cached?.lat ?: 0.0,
                lon = cached?.lon ?: 0.0,
                token = apiKey
            )

            if (response.status == "ok" && response.data != null) {
                val entity = AqiEntity(
                    tileId = cellId,
                    aqi = response.data.aqi,
                    dominantPollutant = response.data.dominantPollutant,
                    lastUpdatedTimestamp = now,
                    lat = cached?.lat ?: 0.0,
                    lon = cached?.lon ?: 0.0
                )
                aqiDao.cacheAqi(entity)
                emit(mapEntityToDomain(entity))
            }
        } catch (e: Exception) {
            if (cached != null && now - cached.lastUpdatedTimestamp < STALE_TIMEOUT) {
                emit(mapEntityToDomain(cached))
            } else {
                throw IOException("No valid AQI data available")
            }
        }
    }

    private fun mapEntityToDomain(entity: AqiEntity): AqiSnapshot {
        return AqiSnapshot(
            timestamp = entity.lastUpdatedTimestamp,
            overallAqi = entity.aqi,
            dominantPollutant = PollutantType.PM2_5,
            readings = emptyList(),
            category = determineCategory(entity.aqi),
            healthAdvice = "Data sourced from WAQI"
        )
    }

    private fun determineCategory(aqi: Int): AqiCategory =
        when (aqi) {
            in 0..50 -> AqiCategory.GOOD
            in 51..100 -> AqiCategory.SATISFACTORY
            in 101..200 -> AqiCategory.MODERATE
            in 201..300 -> AqiCategory.POOR
            in 301..400 -> AqiCategory.VERY_POOR
            else -> AqiCategory.SEVERE
        }
}
