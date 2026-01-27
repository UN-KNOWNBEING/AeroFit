package com.aerofit.india.domain.model.aqi

enum class PollutantType(val displayName: String, val unit: String) {
    PM2_5("PM2.5", "µg/m³"),
    PM10("PM10", "µg/m³"),
    NO2("Nitrogen Dioxide", "µg/m³"),
    NH3("Ammonia", "µg/m³"),
    SO2("Sulfur Dioxide", "µg/m³"),
    CO("Carbon Monoxide", "mg/m³"),
    O3("Ozone", "µg/m³")
}
