package com.example.catashtope.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.catashtope.ToDoViewModel
import com.example.catashtope.model.ToDo
import com.example.catashtope.repository.TouristSpot
import com.example.catashtope.repository.TouristSpotRepository
import java.util.Calendar
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ToDoFormPage(
    toDoViewModel: ToDoViewModel,
    onDone: () -> Unit,
    isEdit: Boolean,
    existingToDo: ToDo? = null
) {
    var title by remember { mutableStateOf(existingToDo?.title ?: "") }
    var date by remember { mutableStateOf(existingToDo?.date ?: "") }
    var tempatWisata by remember { mutableStateOf(existingToDo?.tempatWisata ?: "") }
    var tempatQuery by remember { mutableStateOf("") }
    var tempatSuggestions: List<TouristSpot> by remember { mutableStateOf(listOf<TouristSpot>()) }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    // Show DatePickerDialog when this is true
    var showDatePicker by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf(existingToDo?.latitude) }
    var longitude by remember { mutableStateOf(existingToDo?.longitude) }
    var locationName by remember { mutableStateOf("") }
    var showMap by remember { mutableStateOf(false) }

    LaunchedEffect(tempatQuery) {
        if (tempatQuery.isNotBlank()) {
            tempatSuggestions = TouristSpotRepository.fetchTouristSpots(tempatQuery)
        } else {
            tempatSuggestions = emptyList()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val picked = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                date = picked
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEdit) "Edit Trip" else "Add Trip") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (isEdit && existingToDo != null) {
                    toDoViewModel.updateToDo(
                        existingToDo.id, title, date, tempatWisata, latitude, longitude
                    )
                } else {
                    toDoViewModel.addToDo(
                        title, date, tempatWisata, latitude, longitude
                    )
                }
                onDone()
            }) { Text("Save") }
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(24.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Trip Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = date,
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick date"
                        )
                    }
                }
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = tempatQuery,
                onValueChange = {
                    tempatQuery = it
                    tempatWisata = it
                    // Reset coordinates when user types a new query
                    latitude = null
                    longitude = null
                    locationName = ""
                },
                label = { Text("Tempat Wisata (search)") },
                modifier = Modifier.fillMaxWidth()
            )
            if (tempatSuggestions.isNotEmpty()) {
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column {
                        tempatSuggestions.take(5).forEach { suggestion ->
                            Text(
                                suggestion.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempatWisata = suggestion.name
                                        tempatQuery = suggestion.name
                                        // Sync coordinates and locationName from suggestion
                                        latitude = suggestion.latitude
                                        longitude = suggestion.longitude
                                        locationName = suggestion.name
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = { showMap = true }, modifier = Modifier.fillMaxWidth()) {
                Text(if (latitude != null && longitude != null) "Location Selected: $latitude, $longitude" else "Pick Location on Map")
            }
        }
    }
    // Sync map selection with search fields
    if (showMap) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            MapScreen(
                onLocationSelected = { lat, lon, name ->
                    latitude = lat
                    longitude = lon
                    locationName = name ?: ""
                    // Sync search fields with map selection
                    if (!name.isNullOrBlank()) {
                        tempatQuery = name
                        tempatWisata = name
                    }
                    showMap = false
                },
                showBack = true,
                onBack = { showMap = false }
            )
        }
    }
}