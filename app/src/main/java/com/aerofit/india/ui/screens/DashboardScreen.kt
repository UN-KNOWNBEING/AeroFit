package com.aerofit.india.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.MainViewModel
import com.aerofit.india.ui.DashboardUiState

@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115)) // Deep Dark Blue/Black
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("WELCOME BACK,", color = Color.Gray, fontSize = 12.sp)
                Text(viewModel.userName.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            // Rank Badge
            Surface(
                color = Color(0xFF2D3142),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RANK: SCOUT", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00E676))
                }
            }
            is DashboardUiState.Error -> {
                Text("SYSTEM OFFLINE", color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is DashboardUiState.Success -> {
                // --- MAIN CIRCULAR GAUGE ---
                Box(contentAlignment = Alignment.Center) {
                    val aqiColor = if (state.canRun) Color(0xFF00E676) else Color(0xFFFF5252)

                    // Animated Circle
                    CircularIndicator(
                        percentage = (state.currentCell.aqiSnapshot?.overallAqi ?: 0) / 500f,
                        color = aqiColor,
                        size = 220.dp
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AIR QUALITY", color = Color.Gray, fontSize = 10.sp, letterSpacing = 2.sp)
                        Text(
                            text = "${state.currentCell.aqiSnapshot?.overallAqi ?: "--"}",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Surface(
                            color = aqiColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if(state.canRun) "OPTIMAL" else "HAZARD",
                                color = aqiColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- STATS GRID ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Card 1: Score
                    StatCard(
                        title = "TOTAL XP",
                        value = "${state.totalScore}",
                        color = Color(0xFF29B6F6),
                        modifier = Modifier.weight(1f)
                    )
                    // Card 2: Potential
                    StatCard(
                        title = "TILE VALUE",
                        value = "+${state.potentialPoints}",
                        color = Color(0xFFFFD700),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- ACTION BUTTON ---
                Button(
                    onClick = { /* TODO: Trigger Run Logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = if(state.canRun) Color(0xFF00E676) else Color(0xFF455A64)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if(state.canRun) Icons.Default.PlayArrow else Icons.Default.Warning, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if(state.canRun) "START MISSION" else "CONDITIONS UNSAFE",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CircularIndicator(percentage: Float, color: Color, size: Dp) {
    Canvas(modifier = Modifier.size(size)) {
        // Background track
        drawArc(
            color = Color(0xFF2D3142),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )
        // Foreground progress
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 270f * percentage,
            useCenter = false,
            style = Stroke(width = 20f, cap = StrokeCap.Round)
        )
    }
}