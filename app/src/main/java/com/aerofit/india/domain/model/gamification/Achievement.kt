package com.aerofit.india.domain.model.gamification

enum class AchievementCategory {
    FIRST_STEPS, DISTANCE, STREAKS, HAZARD, TILES, CHALLENGE
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val iconEmoji: String
)