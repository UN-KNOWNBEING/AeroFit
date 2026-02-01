package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.geo.GridCell
import kotlin.math.floor

object GridCalculator {
    private const val TILE_SIZE_DEGREES = 0.0045

    fun getCellForLocation(lat: Double, lon: Double): GridCell {
        val latIndex = floor(lat / TILE_SIZE_DEGREES).toInt()
        val lonIndex = floor(lon / TILE_SIZE_DEGREES).toInt()
        val tileId = "${latIndex}_${lonIndex}"

        val centerLat = (latIndex * TILE_SIZE_DEGREES) + (TILE_SIZE_DEGREES / 2)
        val centerLon = (lonIndex * TILE_SIZE_DEGREES) + (TILE_SIZE_DEGREES / 2)

        // Now this works because GridCell matches this constructor
        return GridCell(tileId, centerLat, centerLon, null)
    }
}