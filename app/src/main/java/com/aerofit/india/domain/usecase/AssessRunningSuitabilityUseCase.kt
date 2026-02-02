package com.aerofit.india.domain.usecase

import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile

// Restoring the Advanced Assessment Model
data class Assessment(
    val isSafe: Boolean,
    val message: String,
    val potentialPoints: Int,
    val achievementUnlocked: String? = null
)

class AssessRunningSuitabilityUseCase {
    operator fun invoke(user: UserProfile, cell: GridCell): Assessment {
        val aqi = cell.aqiSnapshot?.overallAqi ?: return Assessment(false, "No Data", 0)

        return when {
            aqi <= 50 -> Assessment(true, "Pure Air! Maximum Points.", 100, "🌿 Forest Breather")
            aqi <= 100 -> Assessment(true, "Good conditions. Go for it!", 80)
            aqi <= 200 -> {
                // Using 'hasAsthma' to match UserProfile
                if (user.hasAsthma) {
                    Assessment(false, "High risk for you. Low reward.", 20)
                } else {
                    Assessment(true, "Moderate pollution. Reduced points.", 50)
                }
            }
            else -> Assessment(false, "Hazardous! Minimal survival points.", 10, "😷 Iron Lung")
        }
    }
}