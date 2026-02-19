package com.aerofit.india.domain.usecase

import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile

// The blueprint for the Advice the app gives you
data class SuitabilityAssessment(
    val isSafe: Boolean,
    val message: String,
    val potentialPoints: Int
)

class AssessRunningSuitabilityUseCase {
    operator fun invoke(user: UserProfile, cell: GridCell): SuitabilityAssessment {
        val aqi = cell.aqiSnapshot?.overallAqi ?: 0
        val isAsthmatic = user.hasRespiratoryIssues

        return when {
            aqi <= 50 -> SuitabilityAssessment(true, "Excellent air quality. Perfect for a run!", 15)
            aqi in 51..100 -> {
                if (isAsthmatic) SuitabilityAssessment(true, "Moderate air. Keep your inhaler handy.", 10)
                else SuitabilityAssessment(true, "Good air quality. Safe to run.", 10)
            }
            aqi in 101..150 -> {
                if (isAsthmatic) SuitabilityAssessment(false, "Unhealthy for sensitive groups. Try indoor HIIT.", 0)
                else SuitabilityAssessment(true, "Fair air. Limit intense exertion.", 5)
            }
            else -> SuitabilityAssessment(false, "Hazardous air! Do not run outside.", 0)
        }
    }
}