package com.aerofit.india.domain.model.user

data class UserProfile(
    val id: String,
    // FIX: Renamed from 'hasAsthma' to 'hasRespiratoryIssues' to match MainViewModel
    val hasRespiratoryIssues: Boolean,
    val age: Int
)