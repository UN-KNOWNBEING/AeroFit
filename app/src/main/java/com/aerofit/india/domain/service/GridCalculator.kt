package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.geo.BoundingBox
import com.aerofit.india.domain.model.geo.Coordinate
import kotlin.math.*

class GridCalculator {

    companion object {
        private const val EARTH_RADIUS_METERS = 6_378_137.0
        private const val GRID_SIZE_METERS = 500.0
        private const val METERS_PER_DEG_LAT = 111_132.0
    }

    fun calculateCellBounds(center: Coordinate): BoundingBox {
        val half = GRID_SIZE_METERS / 2.0
        val dLat = half / METERS_PER_DEG_LAT

        val latRad = Math.toRadians(center.latitude)
        val metersPerDegLon = METERS_PER_DEG_LAT * cos(latRad)
        val dLon = if (abs(metersPerDegLon) > 0.001) half / metersPerDegLon else 0.0

        return BoundingBox(
            southWest = Coordinate(center.latitude - dLat, center.longitude - dLon),
            northEast = Coordinate(center.latitude + dLat, center.longitude + dLon)
        )
    }

    fun calculateDistanceMeters(p1: Coordinate, p2: Coordinate): Double {
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(p1.latitude)) *
                cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }
}
