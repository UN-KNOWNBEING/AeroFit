package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.aqi.*
import kotlin.math.roundToInt

class AqiCalculator {

    private data class Breakpoint(
        val cLow: Double,
        val cHigh: Double,
        val iLow: Int,
        val iHigh: Int
    )

    private val pm25 = listOf(
        Breakpoint(0.0, 30.0, 0, 50),
        Breakpoint(31.0, 60.0, 51, 100),
        Breakpoint(61.0, 90.0, 101, 200),
        Breakpoint(91.0, 120.0, 201, 300),
        Breakpoint(121.0, 250.0, 301, 400),
        Breakpoint(251.0, 1000.0, 401, 500)
    )

    fun calculateAqi(readings: List<PollutantReading>): Pair<Int, PollutantType?> {
        var max = 0
        var dominant: PollutantType? = null

        for (r in readings) {
            val sub = calculateSubIndex(r)
            if (sub > max) {
                max = sub
                dominant = r.type
            }
        }
        return max to dominant
    }

    private fun calculateSubIndex(reading: PollutantReading): Int {
        val bp = when (reading.type) {
            PollutantType.PM2_5 -> pm25
            else -> return 0
        }.find { reading.value in it.cLow..it.cHigh } ?: return 500

        val index = ((bp.iHigh - bp.iLow).toDouble() /
                (bp.cHigh - bp.cLow)) *
                (reading.value - bp.cLow) + bp.iLow

        return index.roundToInt()
    }

    fun getCategory(aqi: Int): AqiCategory =
        AqiCategory.values().find { aqi in it.min..it.max } ?: AqiCategory.SEVERE
}
