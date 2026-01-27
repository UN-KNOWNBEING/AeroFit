package com.aerofit.india.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aerofit.india.ui.*

@Composable
fun DashboardScreen(viewModel: MainViewModel) {

    val state by viewModel.uiState.collectAsState()

    when (state) {
        is DashboardUiState.Loading ->
            Text("Loading...")

        is DashboardUiState.Error ->
            Text((state as DashboardUiState.Error).message)

        is DashboardUiState.Success ->
            Text("Success")
    }
}
