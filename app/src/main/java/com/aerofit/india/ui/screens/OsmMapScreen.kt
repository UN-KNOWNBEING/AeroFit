package com.aerofit.india.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
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

    // Initialize OSM Configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState is DashboardUiState.Success) {
            val state = uiState as DashboardUiState.Success

            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(18.0)
                    }
                },
                update = { mapView ->
                    val userPoint = GeoPoint(state.latitude, state.longitude)

                    // Center map on user
                    mapView.controller.setCenter(userPoint)

                    // Clear old overlays
                    mapView.overlays.clear()

                    // 1. Draw Territory Polygon (500m box)
                    // 0.0045 degrees is roughly 500m
                    val offset = 0.00225
                    val territory = Polygon().apply {
                        points = listOf(
                            GeoPoint(state.latitude + offset, state.longitude - offset),
                            GeoPoint(state.latitude + offset, state.longitude + offset),
                            GeoPoint(state.latitude - offset, state.longitude + offset),
                            GeoPoint(state.latitude - offset, state.longitude - offset)
                        )
                        fillPaint.color = if (state.canRun) 0x4400FF00 else 0x44FF0000 // ARGB Hex
                        outlinePaint.color = if (state.canRun) 0xFF00FF00.toInt() else 0xFFFF0000.toInt()
                        outlinePaint.strokeWidth = 5f
                        title = "Your Territory"
                    }
                    mapView.overlays.add(territory)

                    // 2. Add User Marker
                    val marker = Marker(mapView).apply {
                        position = userPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "You are here"
                        snippet = "Zone: ${state.currentCell.id}"
                    }
                    mapView.overlays.add(marker)

                    mapView.invalidate() // Refresh map
                },
                modifier = Modifier.fillMaxSize()
            )

            // HUD Overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(Color(0xAA000000))
                    .padding(8.dp)
            ) {
                Text("OPENSTREETMAP MODE", color = Color.Yellow, style = MaterialTheme.typography.labelSmall)
                Text("ZONE: ${state.currentCell.id}", color = Color.White, style = MaterialTheme.typography.bodyLarge)
            }

        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Waiting for GPS...", color = Color.Gray)
            }
        }
    }
}