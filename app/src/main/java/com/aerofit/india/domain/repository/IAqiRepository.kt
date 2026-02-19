package com.aerofit.india.domain.repository

import com.aerofit.india.domain.model.aqi.AqiSnapshot

interface IAqiRepository {
    suspend fun getAqiForLocation(lat: Double, lon: Double): Result<AqiSnapshot>
}