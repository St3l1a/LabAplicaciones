package com.example.grocersync.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val listaId: Int = 0, // relación con Lista
    val nombre: String = "",
    val cantidad: Int= 0,
    val categoria: String = "",
    val comprado: Boolean = false,
    val nota: String = ""
)