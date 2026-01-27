package com.aerofit.india.domain.model.aqi

data class PollutantReading(
    val type: PollutantType,
    val value: Double,
    val timestamp: Long
)
