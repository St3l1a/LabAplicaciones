package com.example.grocersync.database


import androidx.room.Embedded
import androidx.room.Relation

data class ListaConItems(
    @Embedded val lista: Lista,

    @Relation(
        parentColumn = "id",
        entityColumn = "listaId"
    )
    val items: List<Item>
)