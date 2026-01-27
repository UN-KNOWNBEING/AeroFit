package com.aerofit.india.domain.usecase

import com.aerofit.india.domain.model.geo.*
import com.aerofit.india.domain.repository.*
import com.aerofit.india.domain.service.GridCalculator
import kotlinx.coroutines.flow.*

class GetAqiForCurrentLocationUseCase(
    private val gridRepo: IGridRepository,
    private val aqiRepo: IAqiRepository,
    private val gridCalculator: GridCalculator
) {
    operator fun invoke(location: Coordinate): Flow<Result<GridCell>> = flow {
        try {
            val bounds = gridCalculator.calculateCellBounds(location)
            val id = "cell_${bounds.center.latitude}_${bounds.center.longitude}"

            var cell = gridRepo.getCellById(id)
            if (cell == null) {
                cell = GridCell(id, bounds)
                gridRepo.saveCell(cell)
            }

            val currentCell = cell
            emitAll(
                aqiRepo.observeAqiForCell(id).map {
                    Result.success(currentCell.copy(aqiSnapshot = it))
                }
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
