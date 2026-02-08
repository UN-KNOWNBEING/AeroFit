package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.gamification.Achievement
import com.aerofit.india.domain.model.gamification.AchievementCategory
import com.aerofit.india.domain.model.user.UserProfile

object AchievementSystem {

    private val allAchievements = listOf(
        Achievement("fs_1", "First Breath", "Complete your first tracked activity.", AchievementCategory.FIRST_STEPS, 50, iconEmoji = "🌱"),
        Achievement("fs_2", "Out the Door", "Walk or run 1 km in a session.", AchievementCategory.FIRST_STEPS, 100, iconEmoji = "🚪"),
        Achievement("fs_3", "Momentum", "Reach 10 total kilometers.", AchievementCategory.FIRST_STEPS, 200, iconEmoji = "🎢"),
        Achievement("dist_1", "5K Rookie", "Cover 5 km in one session.", AchievementCategory.DISTANCE, 300, iconEmoji = "🏃"),
        Achievement("dist_2", "10K Finisher", "Cover 10 km in one session.", AchievementCategory.DISTANCE, 500, iconEmoji = "🏅"),
        Achievement("dist_3", "Marathon Mindset", "Reach 42 km total distance.", AchievementCategory.DISTANCE, 1000, iconEmoji = "🧠"),
        Achievement("dist_4", "Century Club", "Reach 100 km total distance.", AchievementCategory.DISTANCE, 2000, iconEmoji = "💯"),
        Achievement("tile_1", "First Territory", "Capture your first grid tile.", AchievementCategory.TILES, 100, iconEmoji = "🏳️"),
        Achievement("tile_2", "Explorer", "Capture 5 unique tiles.", AchievementCategory.TILES, 250, iconEmoji = "🗺️"),
        Achievement("tile_3", "City Scout", "Capture 15 tiles.", AchievementCategory.TILES, 500, iconEmoji = "🔭"),
        Achievement("tile_4", "Urban Conqueror", "Capture 50 tiles.", AchievementCategory.TILES, 1000, iconEmoji = "👑"),
        Achievement("haz_1", "Air Aware", "Active on a Moderate AQI day.", AchievementCategory.HAZARD, 150, iconEmoji = "😷"),
        Achievement("haz_2", "Smog Slayer", "Active during poor air quality.", AchievementCategory.HAZARD, 300, iconEmoji = "🌫️")
    )

    fun checkAchievements(user: UserProfile, currentAqi: Int): List<Achievement> {
        val unlocked = mutableListOf<Achievement>()

        if (user.totalDistanceKm >= 0.1) unlocked.add(get("fs_1"))
        if (user.totalDistanceKm >= 1.0) unlocked.add(get("fs_2"))
        if (user.totalDistanceKm >= 10.0) unlocked.add(get("fs_3"))
        if (user.totalDistanceKm >= 42.0) unlocked.add(get("dist_3"))
        if (user.totalDistanceKm >= 100.0) unlocked.add(get("dist_4"))

        if (user.tilesCaptured >= 1) unlocked.add(get("tile_1"))
        if (user.tilesCaptured >= 5) unlocked.add(get("tile_2"))
        if (user.tilesCaptured >= 15) unlocked.add(get("tile_3"))
        if (user.tilesCaptured >= 50) unlocked.add(get("tile_4"))

        if (currentAqi in 101..200) unlocked.add(get("haz_1"))
        if (currentAqi > 200) unlocked.add(get("haz_2"))

        return unlocked
    }

    private fun get(id: String): Achievement {
        return allAchievements.find { it.id == id } ?: allAchievements[0]
    }

    fun getAllWithStatus(user: UserProfile): List<Achievement> {
        val currentlyUnlocked = checkAchievements(user, 0).map { it.id }.toSet()
        return allAchievements.map { it.copy(isUnlocked = currentlyUnlocked.contains(it.id)) }
    }
}