package com.example.catashtope.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.net.URL
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onLocationSelected: (latitude: Double, longitude: Double, name: String?) -> Unit,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(5.0)
                    controller.setCenter(GeoPoint(-2.5489, 118.0149)) // Center on Indonesia
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    // Map is draggable and zoomable by default with setMultiTouchControls(true)
                }.also { mapView = it }
            },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                map.overlays.removeAll { it is Marker }
                map.setOnTouchListener { v, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        val proj = map.projection
                        val geoPoint = proj.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        marker?.let { map.overlays.remove(it) }
                        val newMarker = Marker(map).apply {
                            position = geoPoint
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            title = "Selected Location"
                        }
                        map.overlays.add(newMarker)
                        marker = newMarker
                        selectedName = null
                        v.performClick()
                    }
                    false
                }
            }
        )

        // Overlay: Search bar at the top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            if (showBack && onBack != null) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("← Back")
                }
                Spacer(Modifier.height(8.dp))
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search location") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row {
                Button(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            val map = mapView
                            if (map != null) {
                                coroutineScope.launch {
                                    val result = geocodeLocation(searchQuery)
                                    result?.let { (lat, lon, name) ->
                                        map.controller.setZoom(15.0)
                                        map.controller.setCenter(GeoPoint(lat, lon))
                                        marker?.let { map.overlays.remove(it) }
                                        val newMarker = Marker(map).apply {
                                            position = GeoPoint(lat, lon)
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            title = name
                                        }
                                        map.overlays.add(newMarker)
                                        marker = newMarker
                                        selectedName = name
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Search")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        marker?.position?.let {
                            onLocationSelected(it.latitude, it.longitude, selectedName)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Select this location")
                }
            }
        }
    }
}

suspend fun geocodeLocation(query: String): Triple<Double, Double, String>? = withContext(Dispatchers.IO) {
    try {
        val url = "https://nominatim.openstreetmap.org/search?q=" + URLEncoder.encode(query, "UTF-8") + "&format=json&limit=1"
        val response = URL(url).readText()
        val jsonArray = org.json.JSONArray(response)
        if (jsonArray.length() > 0) {
            val obj = jsonArray.getJSONObject(0)
            val lat = obj.getDouble("lat")
            val lon = obj.getDouble("lon")
            val displayName = obj.getString("display_name")
            Triple(lat, lon, displayName)
        } else null
    } catch (e: Exception) {
        null
    }
}