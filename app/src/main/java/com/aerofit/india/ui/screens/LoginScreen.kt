package com.aerofit.india.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.MainViewModel

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    var identifier by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(Color(0xFF1E1E1E), Color(0xFF000000)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
            modifier = Modifier.padding(24.dp).fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("AEROFIT SECURE", color = Color(0xFF00E676), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Identity Verification", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(32.dp))

                // Step 1: Identifier Input
                OutlinedTextField(
                    value = identifier,
                    onValueChange = {
                        identifier = it
                        errorMessage = null // Clear error on type
                    },
                    label = { Text("Phone Number or Email") },
                    enabled = !isOtpSent, // Lock after sending OTP
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Step 2: OTP Input (Visible only after sending)
                if (isOtpSent) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { otpInput = it },
                        label = { Text("Enter 4-Digit OTP") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Button
                Button(
                    onClick = {
                        if (!isOtpSent) {
                            // Validate and Send OTP
                            if (viewModel.validateInput(identifier)) {
                                val code = viewModel.generateOtp()
                                isOtpSent = true
                                Toast.makeText(context, "Simulation: Your OTP is $code", Toast.LENGTH_LONG).show()
                            } else {
                                errorMessage = "Invalid Email or 10-digit Phone"
                            }
                        } else {
                            // Verify OTP
                            if (viewModel.verifyOtp(otpInput, identifier)) {
                                // Success! Navigation happens automatically via state
                            } else {
                                errorMessage = "Wrong OTP. Try '1234' or the code shown."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (!isOtpSent) "SEND VERIFICATION CODE" else "CONFIRM LOGIN", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                if (isOtpSent) {
                    TextButton(onClick = { isOtpSent = false; otpInput = "" }) {
                        Text("Change Number/Email", color = Color.Gray)
                    }
                }
            }
        }
    }
}