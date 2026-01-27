package com.aerofit.india.domain.repository

import com.aerofit.india.domain.model.geo.GridCell

interface IGridRepository {
    suspend fun getCellById(id: String): GridCell?
    suspend fun getCellForLocation(lat: Double, lon: Double): GridCell?
    suspend fun saveCell(cell: GridCell)
}
