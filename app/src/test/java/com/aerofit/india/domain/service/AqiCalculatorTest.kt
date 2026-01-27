package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.aqi.PollutantReading
import com.aerofit.india.domain.model.aqi.PollutantType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AqiCalculatorTest {

    private val calculator = AqiCalculator()

    @Test
    fun `PM2_5 value 45 gives AQI around 75`() {
        val reading = PollutantReading(
            type = PollutantType.PM2_5,
            value = 45.0,
            timestamp = System.currentTimeMillis()
        )

        val (aqi, dominant) = calculator.calculateAqi(listOf(reading))

        assertTrue(aqi in 70..80)
        assertEquals(PollutantType.PM2_5, dominant)
    }

    @Test
    fun `PM2_5 dominates over PM10`() {
        val pm25 = PollutantReading(
            type = PollutantType.PM2_5,
            value = 80.0,
            timestamp = System.currentTimeMillis()
        )

        val pm10 = PollutantReading(
            type = PollutantType.PM10,
            value = 50.0,
            timestamp = System.currentTimeMillis()
        )

        val (aqi, dominant) = calculator.calculateAqi(listOf(pm25, pm10))

        assertTrue(aqi > 100)
        assertEquals(PollutantType.PM2_5, dominant)
    }
}
