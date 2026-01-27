package com.aerofit.india.domain.engine

import com.aerofit.india.domain.policy.AQICategory
import org.junit.Assert.assertTrue
import org.junit.Test

class DecisionEngineTest {

    @Test
    fun `GOOD AQI with sufficient dwell time allows activity`() {
        val result = DecisionEngine.evaluate(
            aqiCategory = AQICategory.GOOD,
            dwellTimeMs = 600_000L
        )
        assertTrue(result is ActivityDecision.Allowed)
    }

    @Test
    fun `MODERATE AQI reduces scoring`() {
        val result = DecisionEngine.evaluate(
            aqiCategory = AQICategory.MODERATE,
            dwellTimeMs = 600_000L
        )
        assertTrue(result is ActivityDecision.ReducedScoring)
    }

    @Test
    fun `POOR AQI blocks activity`() {
        val result = DecisionEngine.evaluate(
            aqiCategory = AQICategory.POOR,
            dwellTimeMs = 600_000L
        )
        assertTrue(result is ActivityDecision.Blocked)
    }

    @Test
    fun `insufficient dwell time blocks activity`() {
        val result = DecisionEngine.evaluate(
            aqiCategory = AQICategory.GOOD,
            dwellTimeMs = 1_000L
        )
        assertTrue(result is ActivityDecision.Blocked)
    }
}
