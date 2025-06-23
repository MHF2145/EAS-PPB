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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.catashtope.repository.TouristSpot
import com.example.catashtope.repository.TouristSpotRepository

@SuppressLint("MissingPermission", "ClickableViewAccessibility")
@Composable
fun MapScreen(
    onLocationSelected: (latitude: Double, longitude: Double, name: String?) -> Unit,
    showBack: Boolean = false,
    onBack: (() -> Unit)? = null,
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    markerName: String? = null,
    readOnly: Boolean = false
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var searchResults by remember { mutableStateOf<List<TouristSpot>>(emptyList()) }
    var showResults by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    }

    // Set initial marker and center if provided
    LaunchedEffect(initialLatitude, initialLongitude, markerName) {
        val map = mapView
        if (map != null && initialLatitude != null && initialLongitude != null) {
            map.controller.setZoom(15.0)
            map.controller.setCenter(GeoPoint(initialLatitude, initialLongitude))
            marker?.let { map.overlays.remove(it) }
            val newMarker = Marker(map).apply {
                position = GeoPoint(initialLatitude, initialLongitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = markerName ?: "Selected Location"
            }
            map.overlays.add(newMarker)
            marker = newMarker
            selectedName = markerName
        }
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
                }.also { mapView = it }
            },
            modifier = Modifier.fillMaxSize(),
            update = { map ->
                if (!readOnly) {
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
                } else {
                    // Disable touch in readOnly mode
                    map.setOnTouchListener { _, _ -> true }
                }
            }
        )

        if (!readOnly) {
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
                    onValueChange = {
                        searchQuery = it
                        showResults = it.isNotBlank()
                        coroutineScope.launch {
                            searchResults = TouristSpotRepository.fetchTouristSpots(it)
                        }
                    },
                    label = { Text("Search location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                if (showResults && searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(searchResults) { spot ->
                            Text(
                                text = spot.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val map = mapView
                                        if (map != null) {
                                            map.controller.setZoom(15.0)
                                            map.controller.setCenter(GeoPoint(spot.latitude, spot.longitude))
                                            marker?.let { map.overlays.remove(it) }
                                            val newMarker = Marker(map).apply {
                                                position = GeoPoint(spot.latitude, spot.longitude)
                                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                                title = spot.name
                                            }
                                            map.overlays.add(newMarker)
                                            marker = newMarker
                                            selectedName = spot.name
                                            showResults = false
                                            searchQuery = spot.name
                                        }
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Row {
                    Button(
                        onClick = {
                            // No-op: search is now live as user types
                        },
                        modifier = Modifier.weight(1f),
                        enabled = false // Disabled, as search is live
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
}
