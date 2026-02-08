package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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

@Composable
fun OsmMapScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState is DashboardUiState.Success) {
            val state = uiState as DashboardUiState.Success

            // --- THE MAP LAYER ---
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(18.5)
                        // Dark Mode Filter could go here in advanced implementation
                    }
                },
                update = { mapView ->
                    val userPoint = GeoPoint(state.latitude, state.longitude)
                    mapView.controller.setCenter(userPoint)
                    mapView.overlays.clear()

                    // Territory Polygon
                    val offset = 0.00225
                    val territory = Polygon().apply {
                        points = listOf(
                            GeoPoint(state.latitude + offset, state.longitude - offset),
                            GeoPoint(state.latitude + offset, state.longitude + offset),
                            GeoPoint(state.latitude - offset, state.longitude + offset),
                            GeoPoint(state.latitude - offset, state.longitude - offset)
                        )
                        // Cyberpunk colors
                        fillPaint.color = if (state.canRun) 0x2200E676 else 0x22FF5252
                        outlinePaint.color = if (state.canRun) 0xFF00E676.toInt() else 0xFFFF5252.toInt()
                        outlinePaint.strokeWidth = 3f
                    }
                    mapView.overlays.add(territory)

                    // User Marker
                    val marker = Marker(mapView).apply {
                        position = userPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Agent"
                    }
                    mapView.overlays.add(marker)
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            // --- THE HUD OVERLAY (New) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Status Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xCC000000), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("ZONE ID", color = Color.Gray, fontSize = 10.sp)
                        Text(state.currentCell.id, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SIGNAL", color = Color.Gray, fontSize = 10.sp)
                        Text("ONLINE", color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom Stats Panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xEE1E1E1E), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    HudStat("TIME", "00:00")
                    HudStat("DIST", "0.0 km")
                    HudStat("XP", "+${state.potentialPoints}", Color(0xFFFFD700))
                }
            }

        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Text("ESTABLISHING UPLINK...", color = Color(0xFF00E676))
            }
        }
    }
}

@Composable
fun HudStat(label: String, value: String, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}