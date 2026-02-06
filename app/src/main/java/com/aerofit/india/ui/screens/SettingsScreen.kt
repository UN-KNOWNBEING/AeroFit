package com.aerofit.india.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aerofit.india.ui.DashboardUiState
import com.aerofit.india.ui.MainViewModel

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize state with ViewModel data, but allow editing
    var name by remember { mutableStateOf(viewModel.userName) }

    // We need to fetch age from the user profile safely
    var age by remember { mutableStateOf("") }
    var hasRespiratoryIssues by remember { mutableStateOf(false) }

    // Populate fields ONCE when screen loads with current data
    LaunchedEffect(Unit) {
        if (uiState is DashboardUiState.Success) {
            val user = (uiState as DashboardUiState.Success).userProfile
            age = user.age.toString()
            hasRespiratoryIssues = user.hasRespiratoryIssues
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text("User Settings", style = MaterialTheme.typography.headlineMedium, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        // Profile Card
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Identity", color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = {
                        // Only allow numeric input
                        if (it.all { char -> char.isDigit() }) {
                            age = it
                        }
                    },
                    label = { Text("Age (Years)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Health Card
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Medical Profile", color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Respiratory Issues (Asthma)", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = hasRespiratoryIssues,
                        onCheckedChange = { hasRespiratoryIssues = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (name.isNotBlank() && age.isNotBlank()) {
                    viewModel.updateProfile(name, age, hasRespiratoryIssues)
                    Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Name and Age cannot be empty", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("SAVE CHANGES", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("LOG OUT", color = Color.White)
        }
    }
}