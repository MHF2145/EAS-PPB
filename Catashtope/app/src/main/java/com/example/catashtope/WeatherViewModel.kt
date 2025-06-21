package com.example.catashtope

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeatherScreenState(
    val isLoading: Boolean = true,
    val weatherData: WeatherResponse? = null,
    val lastUpdated: Long? = null,
    val error: String? = null
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)

    private val errorState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WeatherScreenState> = combine(
        repository.weatherData,
        repository.lastUpdatedTimestamp,
        errorState
    ) { weather, timestamp, error ->
        WeatherScreenState(
            isLoading = weather == null && error == null,
            weatherData = weather,
            lastUpdated = timestamp,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeatherScreenState()
    )

    val allWeatherData = repository.allWeatherData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun refreshData(city: String = "Surabaya") {
        viewModelScope.launch {
            try {
                errorState.value = null
                repository.refreshWeatherData(city)
            } catch (e: Exception) {
                errorState.value = "Gagal mengambil data: ${e.message}"
            }
        }
    }

    init {
        refreshData()
    }
}
