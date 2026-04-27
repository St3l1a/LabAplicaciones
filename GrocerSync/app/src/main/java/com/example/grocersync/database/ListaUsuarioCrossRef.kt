package com.example.grocersync.database


import androidx.room.Entity

@Entity(primaryKeys = ["listaId", "usuarioId"])
data class ListaUsuarioCrossRef(
    val listaId: Int,
    val usuarioId: Int
)