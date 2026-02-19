package com.aerofit.india.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun OsmMapScreen(viewModel: MainViewModel) {
    val uiState = viewModel.uiState.collectAsState().value

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState is DashboardUiState.Success) {
            val geoPoint = GeoPoint(uiState.latitude, uiState.longitude)

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    Configuration.getInstance().userAgentValue = context.packageName
                    MapView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setTileSource(TileSourceFactory.MAPNIK)
                        controller.setZoom(18.0)
                        controller.setCenter(geoPoint)

                        val marker = Marker(this).apply {
                            position = geoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Agent Location"
                        }
                        overlays.add(marker)
                    }
                },
                update = { view ->
                    view.controller.setCenter(geoPoint)
                    view.overlays.clear()
                    val marker = Marker(view).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "AQI: ${uiState.currentCell.aqiSnapshot?.overallAqi}"
                    }
                    view.overlays.add(marker)
                    view.invalidate()
                }
            )

            // --- MISSION HUD OVERLAY ---
            if (uiState.isMissionActive) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0F1115)), // Semi-transparent black
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("MISSION TIME", color = Color.Gray, fontSize = 10.sp)
                            Text(formatTime(uiState.missionTimeSeconds), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }

                        // Red recording dot
                        Box(modifier = Modifier.size(12.dp).background(Color.Red, RoundedCornerShape(50)))

                        Column(horizontalAlignment = Alignment.End) {
                            Text("DISTANCE", color = Color.Gray, fontSize = 10.sp)
                            Text("${String.format("%.2f", uiState.missionDistanceKm)} km", color = Color(0xFF00E676), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Non-mission HUD
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0F1115)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
                ) {
                    Text(
                        text = "AQI: ${uiState.currentCell.aqiSnapshot?.overallAqi} | ${uiState.advice}",
                        color = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}