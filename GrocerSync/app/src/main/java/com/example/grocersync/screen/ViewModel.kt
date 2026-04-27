package com.example.grocersync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.grocersync.database.*
import kotlinx.coroutines.launch

class ListaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).listaDao()
    private val repository = ListaRepository(dao)

    fun insertarLista(nombre: String, creador: String) {
        viewModelScope.launch {
            repository.insertLista(
                Lista(
                    nombre = nombre,
                    creador = creador,
                    fechaCreacion = System.currentTimeMillis().toString()
                )
            )
        }
    }

    fun insertarItem(item: Item) {
        viewModelScope.launch {
            repository.insertItem(item)
        }
    }

    fun actualizarItem(item: Item) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun eliminarItem(item: Item) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}