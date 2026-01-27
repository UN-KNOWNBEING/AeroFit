package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.geo.Coordinate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GridCalculatorTest {

    private val calculator = GridCalculator()

    @Test
    fun `same coordinate returns same bounding box`() {
        val coord = Coordinate(28.6139, 77.2090) // Delhi

        val box1 = calculator.calculateCellBounds(coord)
        val box2 = calculator.calculateCellBounds(coord)

        assertEquals(box1, box2)
    }

    @Test
    fun `grid width is approximately 500 meters`() {
        val center = Coordinate(0.0, 0.0) // Equator
        val box = calculator.calculateCellBounds(center)

        val distance = calculator.calculateDistanceMeters(
            box.southWest,
            Coordinate(box.southWest.latitude, box.northEast.longitude)
        )

        assertTrue(distance in 480.0..520.0)
    }
}
