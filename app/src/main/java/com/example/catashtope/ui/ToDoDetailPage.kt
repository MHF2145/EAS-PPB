package com.example.catashtope.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catashtope.model.ToDo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDoDetailPage(
    todo: ToDo,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
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
        }
    }
}

