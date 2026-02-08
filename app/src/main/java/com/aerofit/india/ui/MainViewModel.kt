package com.aerofit.india.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile
import com.aerofit.india.domain.service.AchievementSystem
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
import kotlin.math.*

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(
        val currentCell: GridCell,
        val canRun: Boolean,
        val advice: String,
        val potentialPoints: Int,
        val totalScore: Int,
        val achievement: String?, // Latest unlock
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

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val auth: FirebaseAuth = Firebase.auth
    private var currentUser = UserProfile(id = "user_1", hasRespiratoryIssues = false, age = 22)
    var userName = "Rider"
    private var currentTotalScore = 0

    // For distance calculation
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    init {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            userName = firebaseUser.displayName ?: "Rider"
            _isLoggedIn.value = true
        }
    }

    // --- AUTH ---
    fun login(identifier: String, pass: String) {
        if (identifier.isNotBlank()) {
            userName = identifier
            _isLoggedIn.value = true
        }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
    }

    fun updateProfile(name: String, age: String, hasAsthma: Boolean) {
        userName = name
        val ageInt = age.toIntOrNull() ?: 22
        // Preserve stats when updating profile info
        currentUser = currentUser.copy(age = ageInt, hasRespiratoryIssues = hasAsthma)

        val lastState = _uiState.value
        if (lastState is DashboardUiState.Success) {
            updateLiveLocation(lastState.latitude, lastState.longitude)
        }
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userName = auth.currentUser?.displayName ?: "Rider"
                _isLoggedIn.value = true
            }
        }
    }

    // --- LIVE TRACKING & GAMIFICATION ---
    fun updateLiveLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            // Calculate Distance moved since last update
            val distanceMoved = calculateDistance(lastLat, lastLon, lat, lon)
            lastLat = lat
            lastLon = lon

            // Update User Stats
            currentUser = currentUser.copy(
                totalDistanceKm = currentUser.totalDistanceKm + distanceMoved,
                tilesCaptured = currentUser.tilesCaptured + 1 // Simulating 1 tile per update for demo
            )

            getAqiUseCase(lat, lon).collect { result ->
                result.onSuccess { cell ->
                    val assessment = assessSuitabilityUseCase(currentUser, cell)
                    if (assessment.isSafe) currentTotalScore += 5

                    // CHECK ACHIEVEMENTS
                    val unlocks = AchievementSystem.checkAchievements(currentUser, cell.aqiSnapshot?.overallAqi ?: 0)
                    val latestUnlock = if (unlocks.isNotEmpty()) unlocks.last().title else null

                    if (latestUnlock != null) currentTotalScore += 500 // Bonus for unlock

                    _uiState.value = DashboardUiState.Success(
                        currentCell = cell,
                        canRun = assessment.isSafe,
                        advice = assessment.message,
                        potentialPoints = assessment.potentialPoints,
                        totalScore = currentTotalScore,
                        achievement = latestUnlock,
                        userProfile = currentUser,
                        latitude = lat,
                        longitude = lon
                    )
                }
            }
        }
    }

    fun loadDataForLocation(lat: Double, lon: Double) {
        _uiState.value = DashboardUiState.Loading
        updateLiveLocation(lat, lon)
    }

    // Haversine formula for distance in km
    private fun calculateDistance(lat1: Double?, lon1: Double?, lat2: Double, lon2: Double): Double {
        if (lat1 == null || lon1 == null) return 0.0
        val R = 6371 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun getAchievementList() = AchievementSystem.getAllWithStatus(currentUser)

    fun validateInput(input: String) = input.isNotBlank()
    fun generateOtp() = "1234"
    fun verifyOtp(input: String, id: String): Boolean {
        if (input == "1234") { login(id, ""); return true }
        return false
    }
}