package com.example.catashtope

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.catashtope.model.ToDo
import java.text.SimpleDateFormat
import java.util.*

class ToDoViewModel : ViewModel() {
    // In-memory list for demo; replace with persistent storage as needed
    private val _todos = mutableStateListOf<ToDo>()
    val todos: List<ToDo> get() = _todos

    var selectedToDo: ToDo? by androidx.compose.runtime.mutableStateOf(null)
        private set

    fun addToDo(title: String, date: String, tempatWisata: String, lattitude: Double? = null, longitude: Double? = null) {
        _todos.add(ToDo(title = title, date = date, tempatWisata = tempatWisata, latitude = lattitude, longitude = longitude))
    }

    fun updateToDo(id: String, title: String, date: String, tempatWisata: String, latitude: Double? = null, longitude: Double? = null) {
        val idx = _todos.indexOfFirst { it.id == id }
        if (idx != -1) {
            _todos[idx] = ToDo(id, title, date, tempatWisata, latitude, longitude)
        }
    }

    fun deleteToDo(id: String) {
        _todos.removeAll { it.id == id }
    }

    fun selectToDo(id: String) {
        selectedToDo = _todos.find { it.id == id }
    }

    fun clearSelection() {
        selectedToDo = null
    }
}

