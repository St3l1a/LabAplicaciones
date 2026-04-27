package com.example.grocersync.database


import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ListaConUsuarios(
    @Embedded val lista: Lista,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ListaUsuarioCrossRef::class,
            parentColumn = "listaId",
            entityColumn = "usuarioId"
        )
    )
    val usuarios: List<Usuario>
)