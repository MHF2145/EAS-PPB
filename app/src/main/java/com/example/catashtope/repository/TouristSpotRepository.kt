package com.example.catashtope.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TouristSpotRepository {
    // Prepared static data for destination tours
    private val destinations = listOf(
        "Taman Mini Indonesia Indah",
        "Borobudur Temple",
        "Prambanan Temple",
        "Mount Bromo",
        "Kawah Ijen",
        "Raja Ampat",
        "Bali Beach",
        "Komodo Island",
        "Lake Toba",
        "Bunaken Marine Park",
        "Dieng Plateau",
        "Tana Toraja",
        "Wakatobi National Park",
        "Derawan Islands",
        "Belitung Island"
    )

    suspend fun fetchTouristSpots(query: String): List<String> = withContext(Dispatchers.Default) {
        if (query.isBlank()) destinations
        else destinations.filter { it.contains(query, ignoreCase = true) }
    }
}
