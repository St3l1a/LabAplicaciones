// Item.kt
package com.example.grocersync.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String = "",
    val categoria: String ="",
    val cantidad: Int = 0,
    val comprado: Boolean = false,
    val listaId: Int = 9,
    val localImagePath: String? = null   // ← ruta de la imagen en almacenamiento interno
)