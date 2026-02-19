package com.aerofit.india.domain.model.aqi

data class AqiSnapshot(
    val overallAqi: Int,
    val dominantPollutant: String,
    val category: AqiCategory
)