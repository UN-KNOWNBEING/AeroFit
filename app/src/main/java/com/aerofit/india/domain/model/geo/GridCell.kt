package com.aerofit.india.domain.model.geo

import com.aerofit.india.domain.model.aqi.AqiSnapshot

// Defined WITHOUT nested Coordinate class to avoid redeclaration error
data class GridCell(
    val id: String,
    val centerLat: Double,
    val centerLon: Double,
    val aqiSnapshot: AqiSnapshot? = null
)