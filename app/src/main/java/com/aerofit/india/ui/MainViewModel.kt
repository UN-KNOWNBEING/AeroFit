package com.aerofit.india.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
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

    // Firebase
    private val auth: FirebaseAuth = Firebase.auth

    private var currentUser = UserProfile(id = "user_1", hasRespiratoryIssues = false, age = 22)
    var userName = "Rider"
    private var currentTotalScore = 0

    init {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            userName = firebaseUser.displayName ?: "Rider"
            _isLoggedIn.value = true
        }
    }

    // --- AUTH ACTIONS ---

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    userName = user?.displayName ?: "Rider"
                    _isLoggedIn.value = true
                } else {
                    _isLoggedIn.value = false
                }
            }
    }

    fun login(identifier: String, pass: String) {
        if (identifier.isNotBlank()) {
            userName = identifier
            _isLoggedIn.value = true
        }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        currentTotalScore = 0
    }

    // --- PROFILE ACTIONS ---
    fun updateProfile(name: String, age: String, hasAsthma: Boolean) {
        userName = name
        val ageInt = age.toIntOrNull() ?: 22
        currentUser = currentUser.copy(age = ageInt, hasRespiratoryIssues = hasAsthma)

        val lastState = _uiState.value
        if (lastState is DashboardUiState.Success) {
            updateLiveLocation(lastState.latitude, lastState.longitude)
        }
    }

    // --- MAP & DATA ACTIONS ---
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
                        potentialPoints = assessment.potentialPoints, // Now resolved!
                        totalScore = currentTotalScore,
                        achievement = assessment.achievementUnlocked, // Now resolved!
                        userProfile = currentUser,
                        latitude = lat,
                        longitude = lon
                    )
                }.onFailure { }
            }
        }
    }

    fun loadDataForLocation(lat: Double, lon: Double) {
        _uiState.value = DashboardUiState.Loading
        updateLiveLocation(lat, lon)
    }

    fun validateInput(input: String): Boolean {
        return input.isNotBlank()
    }

    fun generateOtp(): String {
        return "1234"
    }

    fun verifyOtp(input: String, identifier: String): Boolean {
        if (input == "1234") {
            login(identifier, "")
            return true
        }
        return false
    }
}