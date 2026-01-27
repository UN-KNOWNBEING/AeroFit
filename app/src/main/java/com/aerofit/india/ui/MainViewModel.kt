package com.aerofit.india.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerofit.india.domain.model.geo.Coordinate
import com.aerofit.india.domain.model.user.UserProfile
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val getAqiUseCase: GetAqiForCurrentLocationUseCase,
    private val assessUseCase: AssessRunningSuitabilityUseCase
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)

    val uiState: StateFlow<DashboardUiState> =
        _uiState.asStateFlow()

    fun loadDataForLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            val coords = Coordinate(lat, lon)

            getAqiUseCase(coords).collect { result ->
                result.fold(
                    onSuccess = { cell ->
                        // In a real app, this would come from a UserRepository
                        val user = UserProfile(
                            age = 28,
                            hasAsthma = false,
                            hasRespiratoryIssues = false
                        )
                        val assessment = assessUseCase(user, cell)
                        
                        _uiState.value = DashboardUiState.Success(
                            currentCell = cell,
                            canRun = assessment.isSafe,
                            advice = assessment.message,
                            scoreMultiplier = 1.0f 
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = DashboardUiState.Error(
                            error.message ?: "Failed to load AQI data"
                        )
                    }
                )
            }
        }
    }

    // TEMP: manual state so UI renders
    fun loadFakeData() {
        _uiState.value = DashboardUiState.Error(
            "Domain wired successfully. UI active."
        )
    }
}
