package com.aerofit.india

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat

import com.aerofit.india.di.AppModule
import com.aerofit.india.domain.service.GridCalculator
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import com.aerofit.india.ui.screens.DashboardScreen
import com.aerofit.india.ui.MainViewModel
import com.aerofit.india.ui.ViewModelFactory
import com.aerofit.india.ui.theme.AeroFitTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: MainViewModel by viewModels {
        val repository = AppModule.provideAqiRepository(applicationContext)
        val getAqiUseCase = GetAqiForCurrentLocationUseCase(repository, GridCalculator)
        val assessUseCase = AssessRunningSuitabilityUseCase()
        ViewModelFactory(getAqiUseCase, assessUseCase)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) getCurrentLocation() else Toast.makeText(this, "Location needed", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkPermissionsAndFetchLocation()

        setContent {
            AeroFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkPermissionsAndFetchLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.loadDataForLocation(location.latitude, location.longitude)
                }
            }
        } catch (e: SecurityException) { }
    }
}