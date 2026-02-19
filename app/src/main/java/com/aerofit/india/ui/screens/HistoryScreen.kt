package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: MainViewModel, onBackClick: () -> Unit) {

    // Automatically fetch history from the database when this screen opens
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    val historyList by viewModel.historyLogs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
    ) {
        // --- TOP APP BAR ---
        TopAppBar(
            title = { Text("MISSION HISTORY", color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E2129))
        )

        // --- LIST OF DAILY RECORDS ---
        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data found. Start a mission today!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historyList) { record ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Date
                            Text(
                                text = "DATE: ${record.dateString}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4 Stats horizontally (Changed to Standard Icons!)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                HistoryStatItem(Icons.Default.PlayArrow, "${String.format("%.2f", record.distanceKm)} km", "Distance", Color(0xFF00E676))
                                HistoryStatItem(Icons.Default.PlayArrow, "${record.steps}", "Steps", Color(0xFF00BFFF))
                                HistoryStatItem(Icons.Default.Star, "${record.caloriesBurned}", "Kcal", Color(0xFFFF5252))
                                HistoryStatItem(Icons.Default.Info, formatHistoryTime(record.activeTimeSeconds), "Time", Color.LightGray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}

fun formatHistoryTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}