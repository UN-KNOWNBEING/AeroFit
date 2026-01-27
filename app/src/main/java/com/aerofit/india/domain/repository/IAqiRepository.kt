package com.aerofit.india.domain.repository

import com.aerofit.india.domain.model.aqi.AqiSnapshot
import kotlinx.coroutines.flow.Flow

interface IAqiRepository {
    fun observeAqiForCell(cellId: String): Flow<AqiSnapshot>
}
