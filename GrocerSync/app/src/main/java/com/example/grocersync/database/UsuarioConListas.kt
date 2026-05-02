package com.example.grocersync.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class UsuarioConListas(
    @Embedded val usuario: Usuario,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ListaUsuarioCrossRef::class,
            parentColumn = "usuarioId",
            entityColumn = "listaId"
        )
    )
    val listas: List<Lista>
)
