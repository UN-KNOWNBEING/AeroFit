package com.aerofit.india.domain.usecase

import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile

data class Assessment(val isSafe: Boolean, val message: String)

class AssessRunningSuitabilityUseCase {
    operator fun invoke(user: UserProfile, cell: GridCell): Assessment {
        val aqi = cell.aqiSnapshot?.overallAqi ?: return Assessment(false, "No Data")

        return when {
            aqi <= 100 -> Assessment(true, "Great conditions for a run!")
            // Checks 'hasRespiratoryIssues'
            aqi <= 200 -> Assessment(!user.hasRespiratoryIssues, if(user.hasRespiratoryIssues) "Risky for sensitive groups" else "Moderate air quality")
            else -> Assessment(false, "Air quality is dangerous. Stay indoors.")
        }
    }
}