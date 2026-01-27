package com.aerofit.india.domain.model.aqi

data class AqiSnapshot(
    val timestamp: Long,
    val overallAqi: Int,
    val dominantPollutant: PollutantType,
    val readings: List<PollutantReading>,
    val category: AqiCategory,
    val healthAdvice: String
)
