package com.aerofit.india.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_history_table")
data class DailyRecordEntity(
    @PrimaryKey
    val dateString: String, // Format: "YYYY-MM-DD"
    val steps: Int = 0,
    val distanceKm: Double = 0.0,
    val caloriesBurned: Int = 0,
    val activeTimeSeconds: Int = 0
)