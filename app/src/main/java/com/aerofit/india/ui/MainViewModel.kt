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

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val currentCell: GridCell,
        val canRun: Boolean,
        val advice: String,
        val potentialPoints: Int,
        val totalScore: Int,
        val achievement: String?,
        val userProfile: UserProfile,
        val latitude: Double,
        val longitude: Double
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class MainViewModel(
    private val getAqiUseCase: GetAqiForCurrentLocationUseCase,
    private val assessSuitabilityUseCase: AssessRunningSuitabilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // --- AUTH STATE ---
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private var currentUser = UserProfile(id = "user_1", hasAsthma = false, age = 22)
    var userName = "Rider"
    private var currentTotalScore = 0

    // OTP Logic
    private var generatedOtp = ""

    // --- AUTH ACTIONS ---

    fun validateInput(input: String): Boolean {
        // Simple regex for Email OR 10-digit Phone
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
        val phoneRegex = "^[0-9]{10}$".toRegex()
        return emailRegex.matches(input) || phoneRegex.matches(input)
    }

    fun generateOtp(): String {
        // Generate a random 4-digit code
        generatedOtp = (1000..9999).random().toString()
        return generatedOtp // Return it so UI can show it in a Toast (Simulation)
    }

    fun verifyOtp(inputOtp: String, identifier: String): Boolean {
        return if (inputOtp == generatedOtp || inputOtp == "1234") { // Backdoor for easy testing
            userName = if(identifier.contains("@")) identifier.substringBefore("@") else "User ${identifier.takeLast(4)}"
            _isLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        currentTotalScore = 0
    }

    // --- PROFILE ACTIONS ---
    fun updateProfile(name: String, age: String, hasAsthma: Boolean) {
        userName = name
        // Robust Integer parsing
        val ageInt = try {
            age.toInt()
        } catch (e: NumberFormatException) {
            currentUser.age // Fallback to old age if invalid
        }

        currentUser = currentUser.copy(age = ageInt, hasAsthma = hasAsthma)

        // Refresh data
        val lastState = _uiState.value
        if (lastState is DashboardUiState.Success) {
            updateLiveLocation(lastState.latitude, lastState.longitude)
        }
    }

    // --- MAP ACTIONS ---
    fun updateLiveLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            getAqiUseCase(lat, lon).collect { result ->
                result.onSuccess { cell ->
                    val assessment = assessSuitabilityUseCase(currentUser, cell)

                    if (assessment.isSafe) {
                        currentTotalScore += 1
                    }

                    _uiState.value = DashboardUiState.Success(
                        currentCell = cell,
                        canRun = assessment.isSafe,
                        advice = assessment.message,
                        potentialPoints = assessment.potentialPoints,
                        totalScore = currentTotalScore,
                        achievement = assessment.achievementUnlocked,
                        userProfile = currentUser,
                        latitude = lat,
                        longitude = lon
                    )
                }.onFailure {
                    // Silent fail
                }
            }
        }
    }

    fun loadDataForLocation(lat: Double, lon: Double) {
        _uiState.value = DashboardUiState.Loading
        updateLiveLocation(lat, lon)
    }
}