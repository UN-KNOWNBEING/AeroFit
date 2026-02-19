package com.aerofit.india.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecordEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val distanceKm: Double,
    val caloriesBurned: Int,
    val activeTimeSeconds: Int,
    val pushupReps: Int = 0,
    val squatReps: Int = 0
)