package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
// IMPORTS ARE CRITICAL HERE
import com.aerofit.india.ui.MainViewModel
import com.aerofit.india.ui.DashboardUiState

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> CircularProgressIndicator()
            is DashboardUiState.Error -> Text("Error: ${state.message}", color = Color.Red)
            is DashboardUiState.Success -> {
                Text(if(state.canRun) "SAFE TO RUN" else "UNSAFE", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(state.advice, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                state.currentCell.aqiSnapshot?.let {
                    Text("AQI: ${it.overallAqi}", color = Color.White)
                }
            }
        }
    }
}