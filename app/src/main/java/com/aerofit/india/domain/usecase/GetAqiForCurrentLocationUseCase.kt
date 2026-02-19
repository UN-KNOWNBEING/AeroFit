package com.aerofit.india.domain.usecase

import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.repository.IAqiRepository
import com.aerofit.india.domain.service.GridCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAqiForCurrentLocationUseCase(
    private val repository: IAqiRepository,
    private val gridCalculator: GridCalculator
) {
    operator fun invoke(lat: Double, lon: Double): Flow<Result<GridCell>> = flow {
        val cellId = gridCalculator.calculateCellId(lat, lon)
        val result = repository.getAqiForLocation(lat, lon)

        result.onSuccess { snapshot ->
            emit(Result.success(GridCell(cellId, lat, lon, snapshot)))
        }.onFailure { error ->
            emit(Result.failure(error))
        }
    }
}