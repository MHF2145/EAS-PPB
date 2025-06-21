package com.example.catashtope

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.catashtope.ui.theme.CatashtopeTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            CatashtopeTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherScreen(weatherViewModel, isDarkTheme) {
                        isDarkTheme = !isDarkTheme
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(viewModel: WeatherViewModel, isDarkTheme: Boolean, onToggleTheme: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val allWeather by viewModel.allWeatherData.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var refreshInMin by remember { mutableStateOf(10) }
    val context = LocalContext.current
    var showBanner by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val editor = prefs.edit()

    var unit by remember { mutableStateOf(prefs.getString("unit", "C") ?: "C") }
    var city by remember { mutableStateOf(prefs.getString("city", "Surabaya") ?: "Surabaya") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var shouldFetchImage by remember { mutableStateOf(false) }

    val hour = LocalTime.now().hour
    if (hour in 5..8) showBanner = true

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L) // wait 1 minute
            refreshInMin--
            if (refreshInMin <= 0) {
                viewModel.refreshData(city)
                refreshInMin = 10
                editor.putLong("last_updated", System.currentTimeMillis()).apply()
            }
        }
    }


    LaunchedEffect(shouldFetchImage, city) {
        if (shouldFetchImage) {
            try {
                val response = PexelsApi.service.searchPhotos(
                    apiKey = "5qZTnjJTJWInzU04Cr96gO427hhF53INCiZBBPs07C5gdaDRFzCyaHkw",
                    query = "$city landscape"
                )
                if (response.isSuccessful) {
                    val url = response.body()?.photos?.firstOrNull()?.src?.landscape
                    imageUrl = url
                    Log.d("PexelsImage", "Image URL: $url")
                } else {
                    Log.e("PexelsImage", "Pexels API failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PexelsImage", "Exception: ${e.message}")
            }
            shouldFetchImage = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showBanner) {
            Text(
                text = "☀️ Selamat pagi! Semoga harimu cerah.",
                color = Color(0xFF0D47A1),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = city,
                onValueChange = {
                    city = it
                    editor.putString("city", it).apply()
                },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                editor.putString("city", city).apply()
                viewModel.refreshData(city)
                refreshInMin = 10
                editor.putLong("last_updated", System.currentTimeMillis()).apply()
                shouldFetchImage = true
            }) {
                Text("Cari")
            }
        }


        when {
            state.weatherData != null -> {
                WeatherCard(
                    data = state.weatherData!!,
                    lastUpdated = prefs.getLong("last_updated", System.currentTimeMillis()),
                    onRefresh = {
                        viewModel.refreshData(city)
                        editor.putLong("last_updated", System.currentTimeMillis()).apply()
                    },
                    onShare = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "Cuaca saat ini di $city: ${state.weatherData!!.current.temperature}°C")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Bagikan cuaca via"))
                    },
                    refreshInSec = refreshInMin,
                    onToggleTheme = onToggleTheme,
                    isDark = isDarkTheme,
                    unit = unit,
                    onToggleUnit = {
                        unit = if (unit == "C") "F" else "C"
                        editor.putString("unit", unit).apply()
                    },
                    city = city
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { showDialog = true }) {
                    Text("Lihat Riwayat Cuaca")
                }

                Spacer(modifier = Modifier.height(16.dp))

                imageUrl?.let {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto pemandangan $city",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(30.dp))
                    )
                }

                if (showDialog) {
                    WeatherHistoryDialog(
                        dataList = allWeather,
                        onDismiss = { showDialog = false },
                        isDarkTheme = isDarkTheme,
                        city = city
                    )
                }
            }

            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                ErrorView(message = state.error!!, onRetry = { viewModel.refreshData(city) })
            }
        }
    }
}

@Composable
fun WeatherCard(
    data: WeatherResponse,
    lastUpdated: Long?,
    onRefresh: () -> Unit,
    onShare: () -> Unit,
    refreshInSec: Int,
    onToggleTheme: () -> Unit,
    isDark: Boolean,
    unit: String,
    onToggleUnit: () -> Unit,
    city: String
) {
    val icon = when {
        data.current.rain > 0 -> "🌧️"
        data.current.snowfall > 0 -> "❄️"
        data.current.isDay == 1 -> "☀️"
        else -> "🌙"
    }

    val temp = data.current.temperature
    val displayTemp = if (unit == "C") temp else (temp * 9 / 5 + 32)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$icon Cuaca Saat Ini di ${data.cityName}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = String.format("%.1f %s", displayTemp, unit),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Auto refresh dalam $refreshInSec menit", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val buttonHeight = 64.dp

                Button(
                    onClick = onRefresh,
                    modifier = Modifier
                        .height(buttonHeight)
                        .weight(1.1f)
                ) {
                    Text("Segarkan", textAlign = TextAlign.Center)
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .height(buttonHeight)
                        .weight(1f)
                ) {
                    Text("Bagikan", textAlign = TextAlign.Center)
                }

                Button(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .height(buttonHeight)
                        .weight(0.9f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(if (isDark) "☀️" else "🌙", fontSize = 18.sp)
                        Text(if (isDark) "Terang" else "Gelap", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            lastUpdated?.let {
                Text(
                    text = "Terakhir diperbarui: ${formatTimestamp(it)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun WeatherHistoryDialog(
    dataList: List<WeatherResponse>,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    city: String
) {
    var filter by remember { mutableStateOf("Semua") }
    val filtered = when (filter) {
        "Siang" -> dataList.filter { it.current.isDay == 1 }
        "Malam" -> dataList.filter { it.current.isDay == 0 }
        else -> dataList
    }
    val maxTemp = dataList.maxOfOrNull { it.current.temperature }
    val minTemp = dataList.minOfOrNull { it.current.temperature }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        },
        title = {
            Column {
                Text("Riwayat Data Cuaca", style = MaterialTheme.typography.titleLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    listOf("Semua", "Siang", "Malam").forEach { label ->
                        FilterChip(selected = filter == label, onClick = { filter = label }, label = { Text(label) })
                    }
                }
            }
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { item ->
                        val icon = when {
                            item.current.rain > 0 -> "🌧️"
                            item.current.snowfall > 0 -> "❄️"
                            item.current.isDay == 1 -> "☀️"
                            else -> "🌙"
                        }
                        val tempFlag = when (item.current.temperature) {
                            maxTemp -> "🔥"
                            minTemp -> "❄️"
                            else -> ""
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFE3F2FD)
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    "${formatISO8601(item.current.time)} $icon",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) MaterialTheme.colorScheme.onSurface else Color(0xFF0D47A1)
                                )
                                Text("Wilayah: ${item.cityName}", color = MaterialTheme.colorScheme.onSurface)
                                Text("Suhu: ${item.current.temperature} ${item.units.temperature} $tempFlag", color = MaterialTheme.colorScheme.onSurface)
                                Text("Hujan: ${item.current.rain} ${item.units.rain}", color = MaterialTheme.colorScheme.onSurface)
                                Text("Salju: ${item.current.snowfall} ${item.units.snowfall}", color = MaterialTheme.colorScheme.onSurface)
                                Text("Waktu: ${if (item.current.isDay == 1) "Siang" else "Malam"}", color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    )
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatISO8601(isoTime: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(isoTime)
        val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        formatter.timeZone = TimeZone.getDefault()
        formatter.format(date!!)
    } catch (e: Exception) {
        isoTime
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Terjadi kesalahan:\n$message",
            color = Color.Red,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Coba Lagi") }
    }
}
