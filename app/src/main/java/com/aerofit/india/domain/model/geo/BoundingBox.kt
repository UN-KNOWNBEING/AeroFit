package com.aerofit.india.domain.model.geo

data class BoundingBox(
    val minLat: Double,
    val minLon: Double,
    val maxLat: Double,
    val maxLon: Double
) {
    // Calculus logic using simple Doubles
    fun center(): Coordinate {
        return Coordinate(
            lat = (minLat + maxLat) / 2.0,
            lon = (minLon + maxLon) / 2.0
        )
    }

    // Logic to check if a point is inside the box
    fun contains(point: Coordinate): Boolean {
        return point.lat >= minLat && point.lat <= maxLat &&
                point.lon >= minLon && point.lon <= maxLon
    }
}