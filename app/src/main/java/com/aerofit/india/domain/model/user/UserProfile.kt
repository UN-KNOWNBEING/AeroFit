package com.aerofit.india.domain.model.user

data class UserProfile(
    val id: String,
    val hasRespiratoryIssues: Boolean, // FIX: Matches the error log requirement
    val age: Int
)