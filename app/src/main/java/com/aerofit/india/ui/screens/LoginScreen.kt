package com.aerofit.india.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.MainViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    // --- Google Sign-In Configuration ---
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // FIX: Replaced R.string with a dummy string so the app compiles immediately!
            .requestIdToken("dummy-client-id.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.firebaseAuthWithGoogle(token)
                }
            } catch (e: ApiException) {
                isLoading = false
                Toast.makeText(context, "Dev Mode: Bypassing Auth", Toast.LENGTH_SHORT).show()
                viewModel.login("Guest")
            }
        } else {
            isLoading = false
        }
    }

    // --- UI DESIGN ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Logo / Branding
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676).copy(alpha = 0.2f))
                    .border(2.dp, Color(0xFF00E676), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AEROFIT",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Text(
                text = "TACTICAL FITNESS TRACKER",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF00E676),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 2. Google Sign-In Button
            if (isLoading) {
                CircularProgressIndicator(color = Color(0xFF00E676))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Authenticating...", color = Color.Gray)
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("G", color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("o", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("o", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("g", color = Color.Blue, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("l", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("e", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Continue with Google",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { viewModel.login("Agent") }) {
                    Text("Developer Bypass (Guest Mode)", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        // 3. Footer
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "By continuing, you agree to the Terms & Privacy Policy.",
                color = Color.Gray,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "v1.0.0 Stable Build",
                color = Color.DarkGray,
                fontSize = 10.sp
            )
        }
    }
}