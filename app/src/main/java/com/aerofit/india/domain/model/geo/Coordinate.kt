package com.aerofit.india.domain.model.geo

data class Coordinate(
    val latitude: Double,
    val longitude: Double
) {
    init {
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90.0 and 90.0 degrees. Provided: $latitude"
        }
        require(longitude in -180.0..180.0) {
            "Longitude must be between -180.0 and 180.0 degrees. Provided: $longitude"
        }
    }

    override fun toString(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return "${kotlin.math.abs(latitude)}° $latDir, ${kotlin.math.abs(longitude)}° $lonDir"
    }
}
