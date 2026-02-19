package com.aerofit.india.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserEntity(
    @PrimaryKey
    val id: String = "user_1",
    val name: String = "Rider",
    val age: Int = 22,
    val hasRespiratoryIssues: Boolean = false,
    val totalDistanceKm: Double = 0.0,
    val tilesCaptured: Int = 0,
    val totalScore: Int = 0
)