package com.aerofit.india

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
// --- CRITICAL IMPORTS FOR MODIFIER ---
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
// -------------------------------------

import com.aerofit.india.di.AppModule
import com.aerofit.india.domain.service.GridCalculator
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase
import com.aerofit.india.ui.AeroFitApp
import com.aerofit.india.ui.MainViewModel
import com.aerofit.india.ui.ViewModelFactory
import com.aerofit.india.ui.theme.AeroFitTheme
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val viewModel: MainViewModel by viewModels {
        val repository = AppModule.provideAqiRepository(applicationContext)
        val getAqiUseCase = GetAqiForCurrentLocationUseCase(repository, GridCalculator)
        val assessUseCase = AssessRunningSuitabilityUseCase()
        ViewModelFactory(getAqiUseCase, assessUseCase)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startLocationUpdates() else Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup Live Location Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    viewModel.updateLiveLocation(location.latitude, location.longitude)
                }
            }
        }

        checkPermissionsAndStart()

        setContent {
            AeroFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), // This requires androidx.compose.ui.Modifier
                    color = MaterialTheme.colorScheme.background
                ) {
                    AeroFitApp(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkPermissionsAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Update every 5 seconds
            fastestInterval = 2000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        // fusedLocationClient.removeLocationUpdates(locationCallback) // Optional battery saving
    }
}