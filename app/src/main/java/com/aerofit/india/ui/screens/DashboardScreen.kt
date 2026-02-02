package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                // 1. Total Score Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL SCORE", color = Color.LightGray, fontSize = 12.sp)
                        Text("${state.totalScore} XP", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    }
                }

                // 2. Main Status
                Text(
                    text = if(state.canRun) "SAFE TO CAPTURE" else "UNSAFE ZONE",
                    color = if(state.canRun) Color.Green else Color.Red,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Points Potential Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Capture Value", color = Color.Gray, fontSize = 12.sp)
                        Text("+${state.potentialPoints}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(state.advice, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))

                // 4. Achievement Unlock Notification
                state.achievement?.let { achievement ->
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF33691E))) {
                        Text(
                            text = "🏆 Achievement Unlocked: $achievement",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}