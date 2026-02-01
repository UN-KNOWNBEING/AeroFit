package com.aerofit.india.domain.model.geo

import com.aerofit.india.domain.model.aqi.AqiSnapshot

data class GridCell(
    val id: String,
    val bounds: BoundingBox,
    val aqiSnapshot: AqiSnapshot? = null
)
