package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.data.local.DailyRecordEntity
import com.aerofit.india.ui.MainViewModel

@Composable
fun HistoryScreen(viewModel: MainViewModel, onBackClick: () -> Unit) {
    val historyLogs by viewModel.historyLogs.collectAsState()

    // Load data when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("MISSION LOGS", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (historyLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No missions recorded yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(historyLogs) { log ->
                    HistoryCard(log)
                }
            }
        }
    }
}

@Composable
fun HistoryCard(log: DailyRecordEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("DATE: ${log.date}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("DISTANCE", color = Color.Gray, fontSize = 10.sp)
                    Text("${String.format("%.2f", log.distanceKm)} km", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STEPS", color = Color.Gray, fontSize = 10.sp)
                    Text("${log.steps}", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CALORIES", color = Color.Gray, fontSize = 10.sp)
                    Text("${log.caloriesBurned} kcal", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HIIT STATS
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("PUSH-UPS", color = Color.Gray, fontSize = 10.sp)
                    Text("${log.pushupReps} Reps", color = Color(0xFF00BFFF), fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SQUATS", color = Color.Gray, fontSize = 10.sp)
                    Text("${log.squatReps} Reps", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}