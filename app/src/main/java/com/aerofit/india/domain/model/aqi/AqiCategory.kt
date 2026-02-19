package com.aerofit.india.domain.model.aqi

enum class AqiCategory(val label: String, val colorHex: String) {
    GOOD("Good", "#00E676"),
    SATISFACTORY("Satisfactory", "#8BC34A"),
    MODERATE("Moderate", "#FFEB3B"),
    POOR("Poor", "#FF9800"),
    VERY_POOR("Very Poor", "#F44336"),
    SEVERE("Severe", "#B71C1C")
}