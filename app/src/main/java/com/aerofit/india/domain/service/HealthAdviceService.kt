package com.aerofit.india.domain.service

import com.aerofit.india.domain.model.aqi.AqiCategory
import com.aerofit.india.domain.model.user.UserProfile

class HealthAdviceService {

    fun generateAdvice(category: AqiCategory, user: UserProfile): String =
        when (category) {
            AqiCategory.GOOD -> "Great day for a run! Air is clean."
            AqiCategory.SATISFACTORY -> "Conditions are acceptable. Enjoy your run."
            AqiCategory.MODERATE ->
                if (user.hasAsthma || user.hasRespiratoryIssues)
                    "Air quality is moderate. Keep your run short or carry medication."
                else "Sensitive individuals should be cautious."
            AqiCategory.POOR ->
                if (user.isSensitiveGroup)
                    "Avoid outdoor running. Try an indoor workout."
                else "Prolonged exertion may cause discomfort."
            AqiCategory.VERY_POOR, AqiCategory.SEVERE ->
                "CRITICAL WARNING: Do not run outdoors."
        }

    private val UserProfile.isSensitiveGroup: Boolean
        get() = hasAsthma || hasRespiratoryIssues || age < 5 || age > 65
}
