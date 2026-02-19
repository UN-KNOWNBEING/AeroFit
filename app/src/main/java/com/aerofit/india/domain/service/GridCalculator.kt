package com.aerofit.india.domain.service

object GridCalculator {
    // This turns your GPS coordinates into a specific "Grid Square ID" on the map
    fun calculateCellId(lat: Double, lon: Double): String {
        val latGrid = (lat * 1000).toInt()
        val lonGrid = (lon * 1000).toInt()
        return "GRID_${latGrid}_${lonGrid}"
    }
}