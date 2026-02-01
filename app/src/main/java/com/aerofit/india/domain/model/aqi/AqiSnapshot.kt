package com.aerofit.india.domain.model.aqi

data class AqiSnapshot(
    val overallAqi: Int,
    val dominantPollutant: String,
    val category: AqiCategory,
    val timestamp: Long
)

// This Enum belongs HERE.
// If you have a separate AqiCategory.kt file, DELETE IT to stop the "Ambiguous" errors.
enum class AqiCategory(val label: String, val colorHex: String) {
    Good("Good", "#00B050"),
    Satisfactory("Satisfactory", "#92D050"),
    Moderate("Moderate", "#FFFF00"),
    Poor("Poor", "#FF9900"),
    VeryPoor("Very Poor", "#FF0000"),
    Severe("Severe", "#C00000"),
    Unknown("Unknown", "#808080")
}