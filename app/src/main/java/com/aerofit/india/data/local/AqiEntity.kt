package com.aerofit.india.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aqi_cache")
data class AqiEntity(
    @PrimaryKey val tileId: String,
    val aqi: Int,
    val dominantPollutant: String,
    val lastUpdatedTimestamp: Long,
    val lat: Double,
    val lon: Double
)
