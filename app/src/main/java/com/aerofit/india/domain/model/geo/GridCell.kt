package com.aerofit.india.domain.model.geo

import com.aerofit.india.domain.model.aqi.AqiSnapshot

// We keep this simple (using Doubles) to match the GridCalculator.
// I removed 'data class Coordinate' from here to fix the Redeclaration error.
data class GridCell(
    val id: String,
    val centerLat: Double,
    val centerLon: Double,
    val aqiSnapshot: AqiSnapshot? = null
)