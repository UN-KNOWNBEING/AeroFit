package com.aerofit.india

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import com.aerofit.india.ui.AeroFitApp
import com.aerofit.india.ui.MainViewModel
import com.aerofit.india.ui.ViewModelFactory
import com.aerofit.india.ui.theme.AeroFitTheme
import com.aerofit.india.data.local.AppDatabase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: MainViewModel by viewModels {
        val repository = AppModule.provideAqiRepository(applicationContext)
        val getAqiUseCase = GetAqiForCurrentLocationUseCase(repository, GridCalculator)
        val assessUseCase = AssessRunningSuitabilityUseCase()
        val database = AppDatabase.getDatabase(applicationContext)

        ViewModelFactory(getAqiUseCase, assessUseCase, database.userDao(), applicationContext)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (locationGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission is required.", Toast.LENGTH_LONG).show()
        }
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
                    AeroFitApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkPermissionsAndFetchLocation() {
        val permissionsToRequest = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
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