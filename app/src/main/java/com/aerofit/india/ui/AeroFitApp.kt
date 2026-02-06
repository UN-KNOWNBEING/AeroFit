package com.aerofit.india.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.aerofit.india.ui.screens.DashboardScreen
import com.aerofit.india.ui.screens.LoginScreen
import com.aerofit.india.ui.screens.OsmMapScreen
import com.aerofit.india.ui.screens.SettingsScreen

@Composable
fun AeroFitApp(viewModel: MainViewModel) {
    // Watch the login state
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    var currentScreen by remember { mutableStateOf(0) }

    if (!isLoggedIn) {
        // 1. Show Login if not authenticated
        LoginScreen(viewModel = viewModel)
    } else {
        // 2. Show Main App if authenticated
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF1E1E1E),
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Dash") },
                        selected = currentScreen == 0,
                        onClick = { currentScreen = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                        label = { Text("Map") },
                        selected = currentScreen == 1,
                        onClick = { currentScreen = 1 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentScreen == 2,
                        onClick = { currentScreen = 2 }
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentScreen) {
                    0 -> DashboardScreen(viewModel)
                    1 -> OsmMapScreen(viewModel)
                    2 -> SettingsScreen(viewModel)
                }
            }
        }
    }
}