package com.aerofit.india.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State is defined here. Ensure this is NOT in MainActivity.
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

class MainViewModel(
    private val getAqiUseCase: GetAqiForCurrentLocationUseCase,
    private val assessSuitabilityUseCase: AssessRunningSuitabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // Using 'hasRespiratoryIssues' to match UserProfile model
    private val dummyUser = UserProfile(id = "user_1", hasRespiratoryIssues = false, age = 22)

    fun loadDataForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            getAqiUseCase(lat, lon).collect { result ->
                result.onSuccess { cell ->
                    val assessment = assessSuitabilityUseCase(dummyUser, cell)

                    _uiState.value = DashboardUiState.Success(
                        currentCell = cell,
                        canRun = assessment.isSafe,
                        advice = assessment.message,
                        scoreMultiplier = if (assessment.isSafe) 1.5f else 0.0f
                    )
                }.onFailure { error ->
                    _uiState.value = DashboardUiState.Error(error.message ?: "Unknown Error")
                }
            }
        }
    }
}