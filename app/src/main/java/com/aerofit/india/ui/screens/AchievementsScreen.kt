package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.domain.model.gamification.Achievement
import com.aerofit.india.ui.MainViewModel

@Composable
fun AchievementsScreen(viewModel: MainViewModel) {
    // This function now exists in MainViewModel
    val achievements = viewModel.getAchievementList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .padding(16.dp)
    ) {
        Text("Mission Awards", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // This items call is now type-safe
            items(achievements) { achievement ->
                AchievementCard(achievement)
            }
        }
    }
}

@Composable
fun AchievementCard(achievement: Achievement) {
    val borderColor = if (achievement.isUnlocked) Color(0xFF00E676) else Color(0xFF2D3142)
    val emoji = if (achievement.isUnlocked) achievement.iconEmoji else "🔒"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
        modifier = Modifier.fillMaxWidth().border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(achievement.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${achievement.xpReward} XP", color = Color(0xFFFFD700), fontSize = 12.sp)
        }
    }
}