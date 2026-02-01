package com.aerofit.india.domain.repository

import com.aerofit.india.domain.model.aqi.AqiSnapshot

interface IAqiRepository {
    // Only ONE simple function needed for the app to run
    suspend fun getAqiForLocation(lat: Double, lon: Double, tileId: String): Result<AqiSnapshot>
}