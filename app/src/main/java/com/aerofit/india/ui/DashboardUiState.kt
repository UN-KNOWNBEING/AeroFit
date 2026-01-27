package com.aerofit.india.ui

import com.aerofit.india.domain.model.geo.GridCell

sealed interface DashboardUiState {
    object Loading : DashboardUiState

    data class Success(
        val currentCell: GridCell,
        val canRun: Boolean,
        val advice: String,
        val scoreMultiplier: Float
    ) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}
