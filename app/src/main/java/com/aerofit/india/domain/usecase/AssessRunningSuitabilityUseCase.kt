package com.aerofit.india.domain.usecase

import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.user.UserProfile
import com.aerofit.india.domain.service.HealthAdviceService

class AssessRunningSuitabilityUseCase(
    private val healthAdviceService: HealthAdviceService
) {
    data class Assessment(
        val isSafe: Boolean,
        val message: String,
        val colorHex: String
    )

    operator fun invoke(user: UserProfile, cell: GridCell): Assessment {
        val snapshot = cell.aqiSnapshot ?: return Assessment(false, "Data unavailable", "#888888")

        val msg = healthAdviceService.generateAdvice(snapshot.category, user)
        val safe = snapshot.category.max <= 200

        return Assessment(safe, msg, snapshot.category.colorHex)
    }
}
