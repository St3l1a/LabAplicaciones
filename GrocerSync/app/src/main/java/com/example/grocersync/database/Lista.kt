package com.example.grocersync.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listas")
data class Lista(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val fechaCreacion: String,
    val idCreador: Int
)