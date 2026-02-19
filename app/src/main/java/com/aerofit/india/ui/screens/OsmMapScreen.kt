package com.aerofit.india.ui.screens

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.viewinterop.AndroidView
import com.aerofit.india.ui.DashboardUiState
import com.aerofit.india.ui.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapScreen(viewModel: MainViewModel) {
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    if (uiState is DashboardUiState.Success) {
        val geoPoint = GeoPoint(uiState.latitude, uiState.longitude)
        val aqiAndroidColor = android.graphics.Color.parseColor(uiState.currentCell.aqiSnapshot?.category?.colorHex ?: "#00E676")

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(18.5)
                        controller.setCenter(geoPoint)

                        // Tactical Inverse Filter
                        val inverseMatrix = ColorMatrix(floatArrayOf(
                            -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                            0.0f, -1.0f, 0.0f, 0.0f, 255f,
                            0.0f, 0.0f, -1.0f, 0.0f, 255f,
                            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
                        ))
                        overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(inverseMatrix))
                    }
                },
                update = { view ->
                    view.controller.animateTo(geoPoint)
                    view.overlays.clear()

                    // --- 1. DRAW GPS BREADCRUMB TRAIL ---
                    if (uiState.pathHistory.isNotEmpty()) {
                        val pathLine = Polyline()
                        pathLine.setPoints(uiState.pathHistory.map { GeoPoint(it.first, it.second) })
                        pathLine.outlinePaint.color = android.graphics.Color.CYAN
                        pathLine.outlinePaint.strokeWidth = 10f
                        view.overlays.add(pathLine)
                    }

                    // --- 2. AQI HAZARD ZONE ---
                    val hazardZone = Polygon()
                    hazardZone.points = Polygon.pointsAsCircle(geoPoint, 80.0)
                    hazardZone.fillPaint.color = aqiAndroidColor
                    hazardZone.fillPaint.alpha = 40
                    hazardZone.outlinePaint.color = aqiAndroidColor
                    hazardZone.outlinePaint.strokeWidth = 3f
                    view.overlays.add(hazardZone)

                    // --- 3. DRAW SUPPLY DROP (LOOT) ---
                    if (uiState.airdropLat != null && uiState.airdropLon != null) {
                        val dropMarker = Marker(view)
                        dropMarker.position = GeoPoint(uiState.airdropLat, uiState.airdropLon)
                        dropMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        dropMarker.title = "SUPPLY DROP: +500 XP"
                        // Sets marker icon to default Android star icon
                        dropMarker.icon = context.getDrawable(android.R.drawable.star_big_on)
                        view.overlays.add(dropMarker)
                    }

                    // --- 4. AGENT MARKER ---
                    val playerMarker = Marker(view)
                    playerMarker.position = geoPoint
                    playerMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    playerMarker.title = "Agent ${viewModel.userName}"
                    view.overlays.add(playerMarker)

                    view.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            // --- HUD OVERLAY ---
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xDD0F1115))
                    .border(2.dp, Color(aqiAndroidColor), RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AQI ZONE", color = Color.Gray, fontSize = 10.sp)
                        Text("${uiState.currentCell.aqiSnapshot?.overallAqi ?: "--"}", color = Color(aqiAndroidColor), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MISSION DISTANCE", color = Color.Gray, fontSize = 10.sp)
                        Text("${String.format("%.2f", uiState.missionDistanceKm)} km", color = Color(0xFF00BFFF), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.isMissionActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (uiState.airdropLat == null) {
                        Button(
                            onClick = { viewModel.spawnAirdrop() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            modifier = Modifier.fillMaxWidth().height(45.dp)
                        ) {
                            Text("SCAN FOR SUPPLY DROP", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("🟢 SUPPLY DROP DETECTED 🟢\nMove to the Star icon to claim 500 XP!", color = Color(0xFF00E676), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("START MISSION FROM DASHBOARD", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF00E676))
        }
    }
}