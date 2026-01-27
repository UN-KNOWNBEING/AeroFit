package com.aerofit.india.domain.model.aqi

enum class AqiCategory(
    val label: String,
    val colorHex: String,
    val min: Int,
    val max: Int
) {
    GOOD("Good", "#00B050", 0, 50),
    SATISFACTORY("Satisfactory", "#92D050", 51, 100),
    MODERATE("Moderate", "#FFFF00", 101, 200),
    POOR("Poor", "#FF9900", 201, 300),
    VERY_POOR("Very Poor", "#FF0000", 301, 400),
    SEVERE("Severe", "#C00000", 401, Int.MAX_VALUE)
}
