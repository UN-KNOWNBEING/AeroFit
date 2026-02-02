package com.aerofit.india.domain.model.user

data class UserProfile(
    val id: String,
    val hasAsthma: Boolean, // Standardized Name (Fixed)
    val age: Int
)