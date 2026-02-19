package com.aerofit.india.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.DashboardUiState
import com.aerofit.india.ui.MainViewModel
import com.aerofit.india.ui.components.AqiGauge

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onRankClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onHiitClick: () -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsState().value


    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is DashboardUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00E676))
                }
            }
            is DashboardUiState.Error -> {
                Text("Error: ${uiState.message}", color = Color.Red)
            }
            is DashboardUiState.Success -> {
                val aqi = uiState.currentCell.aqiSnapshot?.overallAqi ?: 0
                val pol = uiState.currentCell.aqiSnapshot?.dominantPollutant ?: "--"
                val hexString = uiState.currentCell.aqiSnapshot?.category?.colorHex ?: "#808080"
                val aqiColor = Color(android.graphics.Color.parseColor(hexString))

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.weight(1f).padding(end = 8.dp).clickable { onRankClick() }) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("AGENT: ${viewModel.userName.uppercase()}", color = Color.Gray, fontSize = 10.sp)
                            Text("XP: ${uiState.totalScore}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = { onHistoryClick() }, modifier = Modifier.background(Color(0xFF1E2129), RoundedCornerShape(12.dp))) {
                        Icon(Icons.Default.DateRange, contentDescription = "History", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                AqiGauge(aqiValue = aqi, pollutant = pol, aqiColor = aqiColor)
                Spacer(modifier = Modifier.height(16.dp))

                if (!uiState.canRun && !uiState.isMissionActive) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0x33FF0000))) {
                        Text("⚠ HAZARDOUS AIR QUALITY ⚠\nRunning outside will result in a 50% XP Penalty.", color = Color(0xFFFF5252), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
                    }
                } else {
                    Text(uiState.advice, color = Color.LightGray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!uiState.isMissionActive) {
                    Text("Select Mission Target:", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        GoalButton("1 km", uiState.missionGoalKm == 1.0) { viewModel.setMissionGoal(1.0) }
                        GoalButton("3 km", uiState.missionGoalKm == 3.0) { viewModel.setMissionGoal(3.0) }
                        GoalButton("5 km", uiState.missionGoalKm == 5.0) { viewModel.setMissionGoal(5.0) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.isMissionActive) {
                    val progress = (uiState.missionDistanceKm / uiState.missionGoalKm).toFloat().coerceIn(0f, 1f)
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3142)), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column { Text("LIVE TIME", color = Color.Gray, fontSize = 10.sp); Text(formatTime(uiState.missionTimeSeconds), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black) }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("STEPS", color = Color.Gray, fontSize = 10.sp); Text("${uiState.activeSteps}", color = Color(0xFF00BFFF), fontSize = 20.sp, fontWeight = FontWeight.Black) }
                                Column(horizontalAlignment = Alignment.End) { Text("DISTANCE", color = Color.Gray, fontSize = 10.sp); Text("${String.format("%.2f", uiState.missionDistanceKm)} / ${uiState.missionGoalKm} km", color = Color(0xFF00E676), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = if (progress >= 1f) Color(0xFFFFD700) else if (uiState.isHazardousRun) Color.Red else Color(0xFF00E676), trackColor = Color(0xFF1E2129))
                        }
                    }
                    TextButton(onClick = { viewModel.devSimulateMovement() }) { Text("🛠 DEV: Simulate +100m Walk", color = Color.Yellow, fontSize = 12.sp) }
                }

                if (uiState.isMissionActive) {
                    Button(onClick = { viewModel.endMission() }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)), shape = RoundedCornerShape(12.dp)) {
                        Text("EXTRACT / END MISSION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                } else if (uiState.canRun) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.startMission(false) }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)), shape = RoundedCornerShape(12.dp)) { Text("START ${uiState.missionGoalKm.toInt()}km MISSION", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        Button(onClick = { onHiitClick() }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2129)), shape = RoundedCornerShape(12.dp)) { Text("OR START INDOOR HIIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onHiitClick() }, modifier = Modifier.weight(1f).height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BFFF)), shape = RoundedCornerShape(12.dp)) { Text("INDOOR HIIT\n(Normal XP)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center) }
                        Button(onClick = { viewModel.startMission(true) }, modifier = Modifier.weight(1f).height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), border = androidx.compose.foundation.BorderStroke(2.dp, Color.Red), shape = RoundedCornerShape(12.dp)) { Text("FORCE RUN\n(50% Penalty)", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center) }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color(0xFF00E676) else Color(0xFF1E2129), contentColor = if (selected) Color.Black else Color.White), shape = RoundedCornerShape(8.dp)) { Text(text, fontWeight = FontWeight.Bold) }
}

fun formatTime(seconds: Int): String { return String.format("%02d:%02d", seconds / 60, seconds % 60) }