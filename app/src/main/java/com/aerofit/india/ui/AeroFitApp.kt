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
import com.aerofit.india.ui.screens.AchievementsScreen
import com.aerofit.india.ui.screens.DashboardScreen
import com.aerofit.india.ui.screens.HistoryScreen
import com.aerofit.india.ui.screens.HiitScreen
import com.aerofit.india.ui.screens.LoginScreen
import com.aerofit.india.ui.screens.OsmMapScreen
import com.aerofit.india.ui.screens.SettingsScreen

@Composable
fun AeroFitApp(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    // 0=Dash, 1=Map, 2=Profile, 3=Achievements, 4=History, 5=Hiit
    var currentScreen by remember { mutableIntStateOf(0) }

    if (!isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
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
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
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
                // Inside AeroFitApp.kt, update the `when (currentScreen)` block:

                when (currentScreen) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onRankClick = { currentScreen = 3 },
                        onHistoryClick = { currentScreen = 4 },
                        onHiitClick = { currentScreen = 5 } // ADD THIS!
                    )
                    1 -> OsmMapScreen(viewModel)
                    2 -> SettingsScreen(viewModel)
                    3 -> AchievementsScreen(viewModel)
                    4 -> HistoryScreen(viewModel = viewModel, onBackClick = { currentScreen = 0 })
                    5 -> HiitScreen(viewModel = viewModel, onBackClick = { currentScreen = 0 }) // ADD THIS!
                }
            }
        }
    }
}