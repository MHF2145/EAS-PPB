package com.example.catashtope.ui

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catashtope.model.ToDo
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.catashtope.WeatherResponse
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.example.catashtope.DailyForecastResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.catashtope.api.WeatherApi

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoDetailPage(
    todo: ToDo,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var forecastData by remember { mutableStateOf<DailyForecastResponse?>(null) }
    var weatherLoading by remember { mutableStateOf(false) }
    var weatherError by remember { mutableStateOf<String?>(null) }

    // Parse date to yyyy-MM-dd
    val todoDate = remember(todo.date) {
        try {
            // Try to parse with common formats, fallback to today if fail
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            LocalDate.parse(todo.date, formatter).toString()
        } catch (e: Exception) {
            LocalDate.now().toString()
        }
    }

    // Fetch daily forecast for the ToDo date
    LaunchedEffect(todo.latitude, todo.longitude, todoDate) {
        if (todo.latitude != null && todo.longitude != null && todoDate.isNotBlank()) {
            weatherLoading = true
            weatherError = null
            try {
                val response = WeatherApi.retrofitService.getDailyForecast(
                    lat = todo.latitude,
                    lon = todo.longitude,
                    startDate = todoDate,
                    endDate = todoDate
                )
                if (response.isSuccessful && response.body() != null) {
                    forecastData = response.body()
                } else {
                    weatherError = "Failed to fetch forecast: ${response.code()}"
                }
            } catch (e: IOException) {
                weatherError = "Network error: ${e.message}"
            } catch (e: HttpException) {
                weatherError = "HTTP error: ${e.message}"
            } catch (e: Exception) {
                weatherError = "Error: ${e.message}"
            } finally {
                weatherLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Trip Detail") }, navigationIcon = {
                IconButton(onClick = onBack) { Text("←") }
            })
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(onClick = onEdit) { Text("Edit") }
                Spacer(Modifier.width(8.dp))
                FloatingActionButton(onClick = onDelete, containerColor = androidx.compose.ui.graphics.Color.Red) { Text("Del") }
            }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(24.dp)) {
            Text(todo.title, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(8.dp))
            Text("Date: ${todo.date}", fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("Tempat Wisata: ${todo.tempatWisata}", fontSize = 16.sp)
            Spacer(Modifier.height(16.dp))

            // Show map if latitude and longitude are available
            if (todo.latitude != null && todo.longitude != null) {
                Text("Location on Map:", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ToDoLocationMap(
                    latitude = todo.latitude,
                    longitude = todo.longitude,
                    name = todo.tempatWisata
                )
                Spacer(Modifier.height(16.dp))
            }

            // Show weather forecast for the ToDo date
            Text("Weather Forecast (${todo.date}):", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            when {
                weatherLoading -> {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
                weatherError != null -> {
                    Text(weatherError ?: "Unknown error", color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
                forecastData != null -> {
                    DailyWeatherInfo(forecastData!!, todoDate)
                }
                else -> {
                    Text("No weather data available.", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ToDoLocationMap(latitude: Double, longitude: Double, name: String) {
    // Add a Card to provide background and elevation for the map
    Card(
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            MapScreen(
                initialLatitude = latitude,
                initialLongitude = longitude,
                markerName = name,
                readOnly = true,
                onLocationSelected = { _, _, _ -> },
                showBack = false
            )
        }
    }
}

@Composable
fun DailyWeatherInfo(forecast: DailyForecastResponse, date: String) {
    val idx = forecast.daily.time.indexOf(date)
    if (idx != -1) {
        val tMax = forecast.daily.temperatureMax[idx]
        val tMin = forecast.daily.temperatureMin[idx]
        val rain = forecast.daily.precipitationSum[idx]
        val units = forecast.dailyUnits
        val weatherType = when {
            rain > 5 -> "Rainy"
            tMax > 30 -> "Sunny"
            tMax < 25 -> "Cloudy"
            else -> "Partly Cloudy"
        }
        val weatherEmoji = when (weatherType) {
            "Rainy" -> "🌧️"
            "Sunny" -> "☀️"
            "Cloudy" -> "☁️"
            else -> "⛅"
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(weatherEmoji, fontSize = 36.sp, modifier = Modifier.padding(end = 16.dp))
                Column {
                    Text(
                        text = weatherType,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Max: $tMax${units.temperatureMax}  Min: $tMin${units.temperatureMin}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Precipitation: $rain${units.precipitationSum}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                "No forecast for this date.",
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}