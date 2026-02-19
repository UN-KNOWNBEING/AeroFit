package com.aerofit.india.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerofit.india.data.local.DailyRecordEntity
import com.aerofit.india.data.local.UserDao
import com.aerofit.india.data.local.UserEntity
import com.aerofit.india.domain.model.gamification.Achievement
import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile
import com.aerofit.india.domain.service.AchievementSystem
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

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
        val longitude: Double,
        val isMissionActive: Boolean = false,
        val isHazardousRun: Boolean = false,
        val missionTimeSeconds: Int = 0,
        val missionDistanceKm: Double = 0.0,
        val activeSteps: Int = 0,
        val missionGoalKm: Double = 1.0,
        val dailySteps: Int = 0,
        val dailyCalories: Int = 0,
        val dailyDistance: Double = 0.0,
        val dailyActiveTime: Int = 0
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class MainViewModel(
    private val getAqiUseCase: GetAqiForCurrentLocationUseCase,
    private val assessSuitabilityUseCase: AssessRunningSuitabilityUseCase,
    private val userDao: UserDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // --- NEW: HISTORY STATE ---
    private val _historyLogs = MutableStateFlow<List<DailyRecordEntity>>(emptyList())
    val historyLogs: StateFlow<List<DailyRecordEntity>> = _historyLogs.asStateFlow()

    private val auth: FirebaseAuth = Firebase.auth
    private var currentUser = UserProfile(id = "user_1", hasRespiratoryIssues = false, age = 22)
    var userName = "Rider"
    private var currentTotalScore = 0
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    // MISSION STATE
    private var isMissionActive = false
    private var isHazardousRun = false
    private var missionTimeSeconds = 0
    private var missionDistanceKm = 0.0
    private var activeSteps = 0
    private var missionGoalKm = 1.0
    private var timerJob: Job? = null

    // DAILY STATE
    private var dailyDistance = 0.0
    private var dailyActiveTime = 0
    private var dailySteps = 0
    private var dailyCalories = 0

    init {
        viewModelScope.launch {
            val savedDbUser = userDao.getUser()
            if (savedDbUser != null) {
                userName = savedDbUser.name
                currentTotalScore = savedDbUser.totalScore
                currentUser = UserProfile(savedDbUser.id, savedDbUser.hasRespiratoryIssues, savedDbUser.age, savedDbUser.totalDistanceKm, savedDbUser.tilesCaptured)
            }
            val todayDate = getTodayDateString()
            val todayRecord = userDao.getDailyRecord(todayDate)
            if (todayRecord != null) {
                dailyDistance = todayRecord.distanceKm
                dailyActiveTime = todayRecord.activeTimeSeconds
                dailySteps = todayRecord.steps
                dailyCalories = todayRecord.caloriesBurned
            }
            if (auth.currentUser != null) {
                userName = auth.currentUser?.displayName ?: userName
                _isLoggedIn.value = true
            }
        }
    }

    private fun getTodayDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // --- NEW: FETCH HISTORY FUNCTION ---
    fun loadHistory() {
        viewModelScope.launch {
            _historyLogs.value = userDao.getAllHistory()
        }
    }

    fun setMissionGoal(km: Double) {
        if (!isMissionActive) {
            missionGoalKm = km
            forceUiUpdate()
        }
    }

    fun startMission(forceHazardous: Boolean = false) {
        isMissionActive = true
        isHazardousRun = forceHazardous
        missionTimeSeconds = 0
        missionDistanceKm = 0.0
        activeSteps = 0
        startTimer()
        forceUiUpdate()
    }

    fun endMission() {
        isMissionActive = false
        timerJob?.cancel()

        val completionRate = if (missionGoalKm > 0) (missionDistanceKm / missionGoalKm).coerceAtMost(1.0) else 0.0
        var basePoints = (missionDistanceKm * 50).toInt()

        if (completionRate >= 1.0) basePoints += 100
        else basePoints = (basePoints * completionRate).toInt()

        if (isHazardousRun) basePoints /= 2

        currentTotalScore += basePoints

        currentUser = currentUser.copy(
            totalDistanceKm = currentUser.totalDistanceKm + missionDistanceKm,
            tilesCaptured = currentUser.tilesCaptured + (missionDistanceKm * 10).toInt()
        )
        saveAllToDb()

        missionTimeSeconds = 0
        missionDistanceKm = 0.0
        activeSteps = 0
        forceUiUpdate()
    }

    fun devSimulateMovement() {
        if (isMissionActive) {
            val fakeDist = 0.1
            missionDistanceKm += fakeDist
            dailyDistance += fakeDist
            activeSteps = (missionDistanceKm * 1312).toInt()
            dailySteps = (dailyDistance * 1312).toInt()
            dailyCalories = (dailyDistance * 62).toInt()
            forceUiUpdate()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isMissionActive) {
                delay(1000L)
                missionTimeSeconds++
                dailyActiveTime++
                if (missionTimeSeconds % 5 == 0) saveAllToDb()
                forceUiUpdate()
            }
        }
    }

    private fun saveAllToDb() {
        viewModelScope.launch {
            userDao.saveUser(UserEntity(currentUser.id, userName, currentUser.age, currentUser.hasRespiratoryIssues, currentUser.totalDistanceKm, currentUser.tilesCaptured, currentTotalScore))
            userDao.saveDailyRecord(DailyRecordEntity(getTodayDateString(), dailySteps, dailyDistance, dailyCalories, dailyActiveTime))
        }
    }

    fun updateLiveLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            val dist = calculateDistance(lastLat, lastLon, lat, lon)
            lastLat = lat
            lastLon = lon

            if (isMissionActive && dist > 0) {
                missionDistanceKm += dist
                dailyDistance += dist
                activeSteps = (missionDistanceKm * 1312).toInt()
                dailySteps = (dailyDistance * 1312).toInt()
                dailyCalories = (dailyDistance * 62).toInt()
            }

            getAqiUseCase(lat, lon).collect { result ->
                result.onSuccess { cell ->
                    val assessment = assessSuitabilityUseCase(currentUser, cell)
                    val unlocks = AchievementSystem.checkAchievements(currentUser, cell.aqiSnapshot?.overallAqi ?: 0)

                    _uiState.value = DashboardUiState.Success(
                        cell, assessment.isSafe, assessment.message, assessment.potentialPoints,
                        currentTotalScore, unlocks.lastOrNull()?.title, currentUser, lat, lon,
                        isMissionActive, isHazardousRun, missionTimeSeconds, missionDistanceKm, activeSteps, missionGoalKm,
                        dailySteps, dailyCalories, dailyDistance, dailyActiveTime
                    )
                }.onFailure { }
            }
        }
    }

    fun loadDataForLocation(lat: Double, lon: Double) {
        _uiState.value = DashboardUiState.Loading
        updateLiveLocation(lat, lon)
    }

    private fun forceUiUpdate() {
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Success) {
            _uiState.value = currentState.copy(
                isMissionActive = isMissionActive, isHazardousRun = isHazardousRun,
                missionTimeSeconds = missionTimeSeconds, missionDistanceKm = missionDistanceKm,
                activeSteps = activeSteps, missionGoalKm = missionGoalKm,
                totalScore = currentTotalScore, userProfile = currentUser,
                dailySteps = dailySteps, dailyCalories = dailyCalories, dailyDistance = dailyDistance, dailyActiveTime = dailyActiveTime
            )
        }
    }

    private fun calculateDistance(lat1: Double?, lon1: Double?, lat2: Double, lon2: Double): Double {
        if (lat1 == null || lon1 == null) return 0.0
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
        return earthRadius * (2 * atan2(sqrt(a), sqrt(1 - a)))
    }

    fun login(id: String) { userName = id; saveAllToDb(); _isLoggedIn.value = true }
    fun logout() { auth.signOut(); _isLoggedIn.value = false }
    fun firebaseAuthWithGoogle(token: String) { auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).addOnCompleteListener { if (it.isSuccessful) { userName = auth.currentUser?.displayName ?: "Rider"; saveAllToDb(); _isLoggedIn.value = true } } }
    fun updateProfile(name: String, age: String, asthma: Boolean) { userName = name; currentUser = currentUser.copy(age = age.toIntOrNull() ?: 22, hasRespiratoryIssues = asthma); saveAllToDb() }
    fun getAchievementList() = AchievementSystem.getAllWithStatus(currentUser)
}