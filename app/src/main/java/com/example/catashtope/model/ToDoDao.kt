package com.example.catashtope.model

import androidx.room.*

@Dao
interface ToDoDao {
    @Query("SELECT * FROM todo_table")
    suspend fun getAll(): List<ToDo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(toDo: ToDo)

    @Delete
    suspend fun delete(toDo: ToDo)
}

