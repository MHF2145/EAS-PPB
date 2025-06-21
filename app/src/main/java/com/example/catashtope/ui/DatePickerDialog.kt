package com.example.catashtope.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DatePickerDialog(onDismissRequest: () -> Unit, onDateChange: (String) -> Unit) {
    val today = remember { java.time.LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onDateChange(selectedDate.toString()) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel") }
        },
        title = { Text("Pick a date") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedDate = selectedDate.minusDays(1) }) { Text("<") }
                Text(selectedDate.toString(), modifier = Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = { selectedDate = selectedDate.plusDays(1) }) { Text(">") }
            }
        }
    )
}

