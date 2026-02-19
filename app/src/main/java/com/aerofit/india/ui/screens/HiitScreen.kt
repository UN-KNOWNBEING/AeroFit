package com.aerofit.india.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.aerofit.india.domain.service.ExerciseType
import com.aerofit.india.domain.service.WorkoutAnalyzer
import com.aerofit.india.ui.MainViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class WorkoutState { SETUP, ACTIVE, RESTING, FINISHED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiitScreen(viewModel: MainViewModel, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Permissions
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    LaunchedEffect(Unit) { if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }

    // Settings State
    var workoutState by remember { mutableStateOf(WorkoutState.SETUP) }
    var selectedExercise by remember { mutableStateOf(ExerciseType.SQUAT) }
    var targetSets by remember { mutableStateOf(3) }
    var targetReps by remember { mutableStateOf(10) }

    // Active Tracker State
    var currentSet by remember { mutableStateOf(1) }
    var currentReps by remember { mutableStateOf(0) }
    var restTimeLeft by remember { mutableStateOf(15) } // 15 sec rest
    var totalWorkoutTime by remember { mutableStateOf(0) }

    // Global Timer
    LaunchedEffect(workoutState) {
        while (workoutState == WorkoutState.ACTIVE || workoutState == WorkoutState.RESTING) {
            delay(1000L)
            totalWorkoutTime++
        }
    }

    // Rest Timer Logic
    LaunchedEffect(workoutState, restTimeLeft) {
        if (workoutState == WorkoutState.RESTING && restTimeLeft > 0) {
            delay(1000L)
            restTimeLeft--
            if (restTimeLeft == 0) {
                currentSet++
                currentReps = 0
                workoutState = WorkoutState.ACTIVE
            }
        }
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { cameraExecutor.shutdown() } }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F1115))) {

        // --- 1. SETUP SCREEN ---
        if (workoutState == WorkoutState.SETUP) {
            // Calculate dynamic XP transparently
            val xpMultiplier = if (selectedExercise == ExerciseType.PUSHUP) 3 else 2
            val totalPotentialXp = targetSets * targetReps * xpMultiplier

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("BUILD MISSION", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(24.dp))

                // Exercise Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ExerciseToggleButton("SQUATS", selectedExercise == ExerciseType.SQUAT) { selectedExercise = ExerciseType.SQUAT }
                    ExerciseToggleButton("PUSH-UPS", selectedExercise == ExerciseType.PUSHUP) { selectedExercise = ExerciseType.PUSHUP }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sets Controller
                Text("SETS: $targetSets", color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = targetSets.toFloat(),
                    onValueChange = { targetSets = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E676), activeTrackColor = Color(0xFF00E676))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reps Controller
                Text("REPS PER SET: $targetReps", color = Color(0xFF00BFFF), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = targetReps.toFloat(),
                    onValueChange = { targetReps = it.toInt() },
                    valueRange = 5f..30f,
                    steps = 24,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E676), activeTrackColor = Color(0xFF00E676))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- NEW: TRANSPARENCY CARD ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI ALGORITHM ACTIVE", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedExercise == ExerciseType.PUSHUP)
                                "Push-ups track elbow angles. Earn 3 XP per rep."
                            else
                                "Squats track knee angles. Earn 2 XP per rep.",
                            color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("POTENTIAL REWARD", color = Color.White, fontSize = 14.sp)
                        Text("+$totalPotentialXp XP", color = Color(0xFF00E676), fontSize = 32.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { workoutState = WorkoutState.ACTIVE },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("START TRACKING", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBackClick) { Text("Cancel", color = Color.Gray) }
            }
        }

        // --- 2. CAMERA TRACKING & REST SCREENS ---
        if (workoutState == WorkoutState.ACTIVE || workoutState == WorkoutState.RESTING) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()
                                .also {
                                    it.setAnalyzer(cameraExecutor, WorkoutAnalyzer(selectedExercise) {
                                        // A REP WAS DETECTED!
                                        if (workoutState == WorkoutState.ACTIVE) {
                                            currentReps++
                                            if (currentReps >= targetReps) {
                                                if (currentSet >= targetSets) {
                                                    workoutState = WorkoutState.FINISHED // Workout done!
                                                } else {
                                                    restTimeLeft = 15 // Trigger rest
                                                    workoutState = WorkoutState.RESTING
                                                }
                                            }
                                        }
                                    })
                                }

                            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                            } catch (e: Exception) { e.printStackTrace() }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark Overlay
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

            // UI Overlay
            Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${selectedExercise.name} MISSION", color = Color(0xFF00E676), fontWeight = FontWeight.Black, fontSize = 24.sp)
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.Close, contentDescription = "Abort", tint = Color.Red) }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (workoutState == WorkoutState.ACTIVE) {
                    // Active Workout UI
                    Text("SET $currentSet OF $targetSets", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.size(200.dp).clip(CircleShape).border(8.dp, Color(0xFF00BFFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$currentReps", color = Color.White, fontSize = 80.sp, fontWeight = FontWeight.Black)
                            Text("OUT OF $targetReps", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                } else if (workoutState == WorkoutState.RESTING) {
                    // Resting UI
                    Text("RECOVERY PROTOCOL", color = Color(0xFFFFD700), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier.size(200.dp).clip(CircleShape).border(8.dp, Color(0xFFFF5252), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$restTimeLeft", color = Color.White, fontSize = 80.sp, fontWeight = FontWeight.Black)
                            Text("SECONDS REST", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(onClick = { restTimeLeft = 1; }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                        Text("SKIP REST", color = Color.White)
                    }
                }
            }
        }

        // --- 3. FINISHED SCREEN ---
        if (workoutState == WorkoutState.FINISHED) {
            val totalCompletedReps = targetSets * targetReps
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = "Star", tint = Color(0xFFFFD700), modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("MISSION ACCOMPLISHED", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(32.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Total Reps: $totalCompletedReps", color = Color.White, fontSize = 20.sp)
                        Text("Exercise: ${selectedExercise.name}", color = Color.Gray, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("XP Earned: +${totalCompletedReps * if (selectedExercise == ExerciseType.PUSHUP) 3 else 2}", color = Color(0xFF00E676), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Calories: ${(totalCompletedReps * 0.5).toInt()} Kcal", color = Color(0xFFFF5252), fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        viewModel.finishHiitSession(totalCompletedReps, selectedExercise.name, totalWorkoutTime)
                        onBackClick()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("EXTRACT & SAVE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun ExerciseToggleButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF00E676) else Color(0xFF1E2129),
            contentColor = if (selected) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(140.dp).height(50.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}