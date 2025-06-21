package com.example.catashtope.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catashtope.ToDoViewModel

@Composable
fun ToDoListPage(toDoViewModel: ToDoViewModel, onToDoClick: (String) -> Unit) {
    val todos = toDoViewModel.todos
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Plan Trip - To Do List", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        if (todos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No trip plans yet. Tap + to add.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(todos) { todo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onToDoClick(todo.id) },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(todo.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Date: ${todo.date}", color = Color.Gray, fontSize = 14.sp)
                            Text("Tempat Wisata: ${todo.tempatWisata}", color = Color(0xFF1976D2), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

