package com.example.grocersync.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.grocersync.database.Lista
import com.example.grocersync.database.ListaRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddListScreen(
    usuarioId: Int,
    repository: ListaRepository,
    onBack: () -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fechaActual = dateFormat.format(Date())

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la lista") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    val nuevaLista = Lista(
                        id = 0, // Room genera automático
                        nombre = nombre,
                        fechaCreacion = fechaActual,
                        idCreador = usuarioId
                    )
                    repository.insertLista(nuevaLista, usuarioId) // Pasamos usuarioId
                    onBack()
                }
            },
            enabled = nombre.isNotBlank()
        ) {
            Text("Guardar lista")
        }
    }
}