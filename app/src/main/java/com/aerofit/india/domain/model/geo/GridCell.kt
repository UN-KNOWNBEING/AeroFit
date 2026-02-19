package com.aerofit.india.domain.model.geo

// FIX: Now it imports perfectly from your aqi folder!
import com.aerofit.india.domain.model.aqi.AqiSnapshot

data class GridCell(
    val id: String,
    val centerLat: Double,
    val centerLon: Double,
    val aqiSnapshot: AqiSnapshot? = null
)