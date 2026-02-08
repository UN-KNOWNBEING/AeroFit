package com.aerofit.india.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
// FIX: Added the missing import below
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.DashboardUiState
import com.aerofit.india.ui.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Default values if loading
    var name by remember { mutableStateOf(viewModel.userName) }
    var age by remember { mutableStateOf("") }
    var hasAsthma by remember { mutableStateOf(false) }

    // Update state when data loads
    LaunchedEffect(uiState) {
        if (uiState is DashboardUiState.Success) {
            val user = (uiState as DashboardUiState.Success).userProfile
            age = user.age.toString()
            hasAsthma = user.hasRespiratoryIssues
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Profile Avatar Placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF2D3142))
                .border(2.dp, Color(0xFF00E676), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (name.isNotEmpty()) name.take(1).uppercase() else "A",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(if (name.isNotEmpty()) name else "Agent", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Level 1 Runner", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(32.dp))

        // Settings Form
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AGENT DETAILS", color = Color(0xFF00E676), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                DarkTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Codename"
                )

                Spacer(modifier = Modifier.height(12.dp))

                DarkTextField(
                    value = age,
                    onValueChange = { if(it.all { c -> c.isDigit() }) age = it },
                    label = "Age"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Respiratory Filter", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Enable if you have asthma", color = Color.Gray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = hasAsthma,
                        onCheckedChange = { hasAsthma = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF00E676)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                viewModel.updateProfile(name, age, hasAsthma)
                Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("UPDATE DATA", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCF6679)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("LOGOUT", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DarkTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF00E676),
            unfocusedBorderColor = Color(0xFF2D3142)
        ),
        modifier = Modifier.fillMaxWidth()
    )
}