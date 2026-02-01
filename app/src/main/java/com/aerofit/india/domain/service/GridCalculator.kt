package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.geo.GridCell
import kotlin.math.floor

object GridCalculator {
    private const val TILE_SIZE_DEGREES = 0.0045

    fun getCellForLocation(lat: Double, lon: Double): GridCell {
        val latIndex = floor(lat / TILE_SIZE_DEGREES).toInt()
        val lonIndex = floor(lon / TILE_SIZE_DEGREES).toInt()
        val tileId = "${latIndex}_${lonIndex}"

        // Calculate center
        val centerLat = (latIndex * TILE_SIZE_DEGREES) + (TILE_SIZE_DEGREES / 2)
        val centerLon = (lonIndex * TILE_SIZE_DEGREES) + (TILE_SIZE_DEGREES / 2)

        // Matches the SIMPLE constructor in File 1 (No BoundingBox needed)
        return GridCell(tileId, centerLat, centerLon, null)
    }
}