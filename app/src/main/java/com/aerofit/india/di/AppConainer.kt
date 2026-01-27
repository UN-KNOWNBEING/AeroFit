package com.aerofit.india.di

import android.content.Context
import androidx.room.Room
import com.aerofit.india.data.local.AppDatabase
import com.aerofit.india.data.remote.WaqiApiService
import com.aerofit.india.data.repository.AqiRepositoryImpl
import com.aerofit.india.domain.repository.IAqiRepository
import com.aerofit.india.domain.repository.IGridRepository
import com.aerofit.india.domain.service.GridCalculator
import com.aerofit.india.domain.service.HealthAdviceService
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import com.aerofit.india.ui.MainViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.aerofit.india.domain.model.geo.GridCell
import com.aerofit.india.domain.model.geo.Coordinate
import kotlinx.coroutines.flow.Flow

class AppContainer(private val context: Context) {

    // ------------------------
    // Network (Retrofit)
    // ------------------------
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.waqi.info/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: WaqiApiService =
        retrofit.create(WaqiApiService::class.java)

    // ------------------------
    // Database (Room)
    // ------------------------
    private val database: AppDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "aerofit_db"
        ).build()

    // ------------------------
    // Services
    // ------------------------
    private val gridCalculator = GridCalculator()
    private val healthAdviceService = HealthAdviceService()

    // ------------------------
    // Repository
    // ------------------------
    private val aqiRepository: IAqiRepository =
        AqiRepositoryImpl(
            apiService = apiService,
            aqiDao = database.aqiDao(),
            apiKey = "YOUR_WAQI_TOKEN"
        )

    private val gridRepository: IGridRepository = object : IGridRepository {
        private val cache = mutableMapOf<String, GridCell>()
        override suspend fun getCellById(id: String): GridCell? = cache[id]
        override suspend fun getCellForLocation(lat: Double, lon: Double): GridCell? {
            return cache.values.find { it.bounds.contains(Coordinate(lat, lon)) }
        }
        override suspend fun saveCell(cell: GridCell) { cache[cell.id] = cell }
    }

    // ------------------------
    // Use cases (Domain)
    // ------------------------
    private val getAqiUseCase =
        GetAqiForCurrentLocationUseCase(
            gridRepo = gridRepository,
            aqiRepo = aqiRepository,
            gridCalculator = gridCalculator
        )

    private val assessSuitabilityUseCase =
        AssessRunningSuitabilityUseCase(healthAdviceService)

    // ------------------------
    // ViewModel
    // ------------------------
    fun provideMainViewModel(): MainViewModel {
        return MainViewModel(
            getAqiUseCase = getAqiUseCase,
            assessUseCase = assessSuitabilityUseCase
        )
    }
}
