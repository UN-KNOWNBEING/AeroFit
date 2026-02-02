package com.aerofit.india.domain.model.geo

import com.aerofit.india.domain.model.aqi.AqiSnapshot

// SIMPLE VERSION: No BoundingBox. Just Center Lat/Lon.
data class GridCell(
    val id: String,
    val centerLat: Double,
    val centerLon: Double,
    val aqiSnapshot: AqiSnapshot? = null
)

// Helper class if needed elsewhere
data class Coordinate(val lat: Double, val lon: Double)