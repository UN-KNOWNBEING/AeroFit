package com.aerofit.india.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aerofit.india.data.local.DailyRecordEntity
import com.aerofit.india.data.local.UserDao
import com.aerofit.india.data.local.UserEntity
import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile
import com.aerofit.india.domain.service.AchievementSystem
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
import kotlin.random.Random

// 1. THE STATE MACHINE (Added Path Tracking & Airdrop!)
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
        val dailyActiveTime: Int = 0,
        val pathHistory: List<Pair<Double, Double>> = emptyList(), // Trail
        val airdropLat: Double? = null, // Loot Box
        val airdropLon: Double? = null
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

class MainViewModel(
    private val getAqiUseCase: GetAqiForCurrentLocationUseCase,
    private val assessSuitabilityUseCase: AssessRunningSuitabilityUseCase,
    private val userDao: UserDao,
    private val context: Context
) : ViewModel(), SensorEventListener {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _historyLogs = MutableStateFlow<List<DailyRecordEntity>>(emptyList())
    val historyLogs: StateFlow<List<DailyRecordEntity>> = _historyLogs.asStateFlow()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var currentUser = UserProfile(id = "user_1", hasRespiratoryIssues = false, age = 22)
    var userName = "Agent"
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

    // NEW GAMIFICATION STATE
    private val pathHistory = mutableListOf<Pair<Double, Double>>()
    private var airdropLat: Double? = null
    private var airdropLon: Double? = null

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
                _isLoggedIn.value = true
            }
            loadDailyData()
        }
    }

    private suspend fun loadDailyData() {
        val todayRecord = userDao.getDailyRecord(getTodayDateString())
        if (todayRecord != null) {
            dailyDistance = todayRecord.distanceKm
            dailyActiveTime = todayRecord.activeTimeSeconds
            dailySteps = todayRecord.steps
            dailyCalories = todayRecord.caloriesBurned
        }
    }

    private fun getTodayDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun loadDataForLocation(lat: Double, lon: Double) {
        updateLiveLocation(lat, lon)
    }

    // --- NEW: SPAWN LOOT BOX ON MAP ---
    fun spawnAirdrop() {
        if (lastLat != null && lastLon != null && airdropLat == null) {
            // Spawn ~150 meters away in a random direction
            val randLat = (Random.nextDouble() - 0.5) * 0.003
            val randLon = (Random.nextDouble() - 0.5) * 0.003
            airdropLat = lastLat!! + randLat
            airdropLon = lastLon!! + randLon
            forceUiUpdate()
        }
    }

    fun devSimulateMovement() {
        if (isMissionActive && lastLat != null && lastLon != null) {
            activeSteps += 130
            dailySteps += 130
            dailyCalories += 5

            // If an airdrop exists, simulate walking TOWARDS it!
            val newLat = if (airdropLat != null) {
                lastLat!! + if (airdropLat!! > lastLat!!) 0.0001 else -0.0001
            } else lastLat!! + 0.0001

            val newLon = if (airdropLon != null) {
                lastLon!! + if (airdropLon!! > lastLon!!) 0.0001 else -0.0001
            } else lastLon!! + 0.0001

            updateLiveLocation(newLat, newLon)
        }
    }

    fun logout() { _isLoggedIn.value = false }

    fun updateProfile(name: String, age: String, asthma: Boolean) {
        userName = name
        currentUser = currentUser.copy(age = age.toIntOrNull() ?: 22, hasRespiratoryIssues = asthma)
        saveAllToDb()
    }

    fun login(id: String) { userName = id; saveAllToDb(); _isLoggedIn.value = true }
    fun firebaseAuthWithGoogle(token: String) { userName = "Google Agent"; saveAllToDb(); _isLoggedIn.value = true }

    fun startMission(forceHazardous: Boolean = false) {
        isMissionActive = true
        isHazardousRun = forceHazardous
        missionTimeSeconds = 0
        missionDistanceKm = 0.0
        activeSteps = 0
        pathHistory.clear()
        airdropLat = null
        airdropLon = null
        stepSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        startTimer()
        forceUiUpdate()
    }

    fun endMission() {
        isMissionActive = false
        timerJob?.cancel()
        sensorManager.unregisterListener(this)

        val points = (missionDistanceKm * 50).toInt() + (activeSteps / 10)
        currentTotalScore += if (isHazardousRun) points / 2 else points
        saveAllToDb()
        forceUiUpdate()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isMissionActive && event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            activeSteps++
            dailySteps++
            if (dailySteps % 20 == 0) dailyCalories++
            val stepDistanceKm = 0.00075
            missionDistanceKm += stepDistanceKm
            dailyDistance += stepDistanceKm
            forceUiUpdate()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun updateLiveLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            val dist = calculateDistance(lastLat, lastLon, lat, lon)
            lastLat = lat
            lastLon = lon

            if (isMissionActive) {
                if (dist > 0) {
                    missionDistanceKm += dist
                    dailyDistance += dist
                }
                // Log the path for the map trail
                pathHistory.add(Pair(lat, lon))

                // CHECK IF AGENT REACHED THE AIRDROP (Within 30 meters / 0.03 km)
                if (airdropLat != null && airdropLon != null) {
                    val dropDist = calculateDistance(lat, lon, airdropLat!!, airdropLon!!)
                    if (dropDist < 0.03) {
                        currentTotalScore += 500 // MASSIVE REWARD!
                        airdropLat = null // Remove from map
                        airdropLon = null
                    }
                }
            }

            getAqiUseCase(lat, lon).collect { result ->
                result.onSuccess { cell ->
                    val assessment = assessSuitabilityUseCase(currentUser, cell)
                    _uiState.value = DashboardUiState.Success(
                        cell, assessment.isSafe, assessment.message, assessment.potentialPoints,
                        currentTotalScore, null, currentUser, lat, lon,
                        isMissionActive, isHazardousRun, missionTimeSeconds, missionDistanceKm, activeSteps, missionGoalKm,
                        dailySteps, dailyCalories, dailyDistance, dailyActiveTime,
                        pathHistory.toList(), airdropLat, airdropLon
                    )
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isMissionActive) {
                delay(1000L)
                missionTimeSeconds++
                dailyActiveTime++
                forceUiUpdate()
            }
        }
    }

    private fun forceUiUpdate() {
        val currentState = _uiState.value
        if (currentState is DashboardUiState.Success) {
            _uiState.value = currentState.copy(
                isMissionActive = isMissionActive, missionTimeSeconds = missionTimeSeconds,
                missionDistanceKm = missionDistanceKm, activeSteps = activeSteps,
                totalScore = currentTotalScore, dailySteps = dailySteps,
                dailyCalories = dailyCalories, dailyDistance = dailyDistance,
                pathHistory = pathHistory.toList(), airdropLat = airdropLat, airdropLon = airdropLon
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

    private fun saveAllToDb() {
        viewModelScope.launch {
            userDao.saveUser(UserEntity(currentUser.id, userName, currentUser.age, currentUser.hasRespiratoryIssues, currentUser.totalDistanceKm, currentUser.tilesCaptured, currentTotalScore))
            userDao.saveDailyRecord(DailyRecordEntity(getTodayDateString(), dailySteps, dailyDistance, dailyCalories, dailyActiveTime))
        }
    }

    fun setMissionGoal(km: Double) { if (!isMissionActive) missionGoalKm = km; forceUiUpdate() }
    fun loadHistory() { viewModelScope.launch { _historyLogs.value = userDao.getAllHistory() } }

    fun finishHiitSession(reps: Int, exerciseName: String, durationSeconds: Int) {
        val earnedXp = reps * (if (exerciseName == "PUSHUP") 3 else 2)
        currentTotalScore += earnedXp
        dailyCalories += (reps * 0.5).toInt()
        dailyActiveTime += durationSeconds
        saveAllToDb()
        forceUiUpdate()
    }

    fun getAchievementList() = AchievementSystem.getAllWithStatus(currentUser)
}