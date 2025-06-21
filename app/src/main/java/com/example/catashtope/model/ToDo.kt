package com.example.catashtope.model

import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

// Data model for a trip to-do item
// id: unique identifier
// title: task title
// date: trip date (ISO string)
// tempatWisata: selected tourist spot

@Entity(tableName = "todo_table")
data class ToDo(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String,
    val tempatWisata: String,
    val latitude: Double?,
    val longitude: Double?
)
