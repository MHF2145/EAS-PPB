package com.example.catashtope.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catashtope.ToDoViewModel
import com.example.catashtope.model.ToDo
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind

@Composable
fun ToDoListPage(toDoViewModel: ToDoViewModel, onToDoClick: (String) -> Unit) {
    val todos = toDoViewModel.todos.sortedBy { it.date }

    // Group todos by formatted date string
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val groupedTodos = todos.groupBy { todo ->
        when (val d = todo.date) {
            is Date -> dateFormat.format(d)
            is Long -> dateFormat.format(Date(d as Long))
            is String -> d
            else -> "-"
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Plan Trip - To Do List",
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (todos.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No trip plans yet. Tap + to add.",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val dateKeys = groupedTodos.keys.toList()
                items(dateKeys.size) { idx ->
                    val date = dateKeys[idx]
                    val agendas = groupedTodos[date] ?: emptyList()
                    TimelineDateSection(
                        date = date,
                        agendas = agendas,
                        onToDoClick = onToDoClick,
                        isFirst = idx == 0,
                        isLast = idx == dateKeys.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineDateSection(
    date: String,
    agendas: List<ToDo>,
    onToDoClick: (String) -> Unit,
    isFirst: Boolean,
    isLast: Boolean
) {
    val timelineColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Timeline indicator for date with connected lines
        Column(
            modifier = Modifier.width(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper connecting line (if not first)
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(16.dp)
                        .background(timelineColor)
                )
            }

            // Timeline circle
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        timelineColor,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )

            // Lower connecting line (if not last)
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(
                            // Calculate height based on content: date text + all agenda cards + spacers
                            32.dp + (88.dp * agendas.size) + (8.dp * (agendas.size - 1)) + 16.dp
                        )
                        .background(timelineColor)
                )
            }
        }

        // Date and agenda cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            Text(
                text = date,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            agendas.forEach { todo ->
                AgendaCardItem(todo = todo, onToDoClick = onToDoClick)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AgendaCardItem(todo: ToDo, onToDoClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToDoClick(todo.id) },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = todo.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = todo.tempatWisata,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 14.sp
                )
            }
        }
    }
}