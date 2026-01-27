package com.aerofit.india.domain.model.geo

data class BoundingBox(
    val southWest: Coordinate,
    val northEast: Coordinate
) {
    init {
        require(southWest.latitude <= northEast.latitude) {
            "SouthWest latitude must be <= NorthEast latitude"
        }
    }

    val center: Coordinate
        get() = Coordinate(
            latitude = (southWest.latitude + northEast.latitude) / 2.0,
            longitude = (southWest.longitude + northEast.longitude) / 2.0
        )

    fun contains(point: Coordinate): Boolean {
        return point.latitude in southWest.latitude..northEast.latitude &&
                point.longitude in southWest.longitude..northEast.longitude
    }
}
