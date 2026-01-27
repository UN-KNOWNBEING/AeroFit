package com.aerofit.india.data.remote

import com.google.gson.annotations.SerializedName

data class WaqiResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: WaqiData?
)

data class WaqiData(
    @SerializedName("aqi") val aqi: Int,
    @SerializedName("idx") val stationId: Int,
    @SerializedName("dominentpol") val dominantPollutant: String
)
