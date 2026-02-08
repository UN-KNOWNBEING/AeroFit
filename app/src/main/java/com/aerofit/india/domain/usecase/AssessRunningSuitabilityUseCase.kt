package com.aerofit.india.domain.usecase

import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile

// --- THIS IS THE SINGLE SOURCE OF TRUTH ---
data class Assessment(
    val isSafe: Boolean,
    val message: String,
    val potentialPoints: Int,
    val achievementUnlocked: String? = null
)

class AssessRunningSuitabilityUseCase {
    operator fun invoke(user: UserProfile, cell: GridCell): Assessment {
        val aqi = cell.aqiSnapshot?.overallAqi ?: return Assessment(false, "Scanning...", 0)

        return when {
            aqi <= 50 -> Assessment(true, "Pure Air! Maximum Points.", 100, "🌿 Forest Breather")
            aqi <= 100 -> Assessment(true, "Good conditions. Go for it!", 80)
            aqi <= 150 -> {
                if (user.hasRespiratoryIssues) {
                    Assessment(false, "Risky for your condition. Stay indoors.", 20)
                } else {
                    Assessment(true, "Unhealthy for sensitive groups. Short sessions only.", 50)
                }
            }
            aqi <= 200 -> Assessment(false, "Unhealthy! Indoor workout recommended.", 30, "🏠 Bunker Mode")
            else -> Assessment(false, "TOXIC HAZARD. Avoid outdoor exercise.", 10, "😷 Survivor")
        }
    }
}