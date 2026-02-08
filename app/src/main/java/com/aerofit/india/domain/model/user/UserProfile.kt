package com.aerofit.india.domain.model.user

data class UserProfile(
    val id: String,
    val hasRespiratoryIssues: Boolean,
    val age: Int,
    // New Stats for Achievements
    val totalDistanceKm: Double = 0.0,
    val tilesCaptured: Int = 0
)