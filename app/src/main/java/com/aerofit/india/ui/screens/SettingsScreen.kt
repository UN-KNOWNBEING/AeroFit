package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // THIS FIXES THE GETVALUE ERROR
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.DashboardUiState
import com.aerofit.india.ui.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState is DashboardUiState.Success) {
        val data = uiState as DashboardUiState.Success

        var name by remember { mutableStateOf(viewModel.userName) }
        var age by remember { mutableStateOf(data.userProfile.age.toString()) }
        var hasAsthma by remember { mutableStateOf(data.userProfile.hasRespiratoryIssues) }

        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F1115)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AGENT PROFILE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Agent Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(unfocusedTextColor = Color.White, focusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = hasAsthma, onCheckedChange = { hasAsthma = it })
                Text("Respiratory Issues / Asthma", color = Color.White)
            }
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.updateProfile(name, age, hasAsthma) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
            ) {
                Text("SAVE PROFILE", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.logout() }) {
                Text("LOGOUT", color = Color.Red)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00E676))
        }
    }
}