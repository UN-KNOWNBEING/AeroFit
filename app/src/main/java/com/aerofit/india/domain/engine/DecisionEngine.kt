package com.aerofit.india.domain.engine

import com.aerofit.india.domain.policy.AQICategory

sealed class ActivityDecision {
    object Allowed : ActivityDecision()
    object ReducedScoring : ActivityDecision()
    data class Blocked(val reason: String) : ActivityDecision()
}

object DecisionEngine {
    const val MIN_DWELL_TIME_MS = 300_000L // 5 minutes

    fun evaluate(
        aqiCategory: AQICategory,
        dwellTimeMs: Long
    ): ActivityDecision {

        if (dwellTimeMs < MIN_DWELL_TIME_MS) {
            return ActivityDecision.Blocked("Insufficient dwell time")
        }

        return when (aqiCategory) {
            AQICategory.GOOD -> ActivityDecision.Allowed
            AQICategory.SATISFACTORY -> ActivityDecision.Allowed
            AQICategory.MODERATE -> ActivityDecision.ReducedScoring
            AQICategory.POOR -> ActivityDecision.Blocked("AQI poor")
            AQICategory.VERY_POOR -> ActivityDecision.Blocked("AQI very poor")
            AQICategory.SEVERE -> ActivityDecision.Blocked("AQI severe")
        }
    }
}
