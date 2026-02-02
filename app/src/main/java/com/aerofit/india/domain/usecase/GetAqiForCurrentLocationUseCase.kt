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
    // Accepts Double (lat, lon) directly
    suspend operator fun invoke(lat: Double, lon: Double): Flow<Result<GridCell>> = flow {
        val cell = gridCalculator.getCellForLocation(lat, lon)
        val result = repository.getAqiForLocation(lat, lon, cell.id)

        result.fold(
            onSuccess = { aqi -> emit(Result.success(cell.copy(aqiSnapshot = aqi))) },
            onFailure = { err -> emit(Result.failure(err)) }
        )
    }
}