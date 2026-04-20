package com.example.grocersync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.grocersync.data.AppDatabase
import com.example.grocersync.ShoppingDao
import kotlinx.coroutines.launch



@Composable
fun TestScreen(db: AppDatabase) {

    val dao = db.shoppingDao()
    val scope = rememberCoroutineScope()

    var lists by remember { mutableStateOf<List<ShoppingList>>(emptyList()) }
    var text by remember { mutableStateOf("") }

    // Cargar listas al iniciar
    LaunchedEffect(Unit) {
        lists = dao.getAllLists()
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Prueba Base de Datos", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Input
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Nombre de la lista") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Botón añadir
        Button(
            onClick = {
                scope.launch {
                    if (text.isNotBlank()) {
                        dao.insertList(ShoppingList(name = text))
                        lists = dao.getAllLists()
                        text = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Añadir lista")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de resultados
        LazyColumn {
            items(lists) { list ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(list.name)

                        Button(onClick = {
                            scope.launch {
                                dao.deleteList(list)
                                lists = dao.getAllLists()
                            }
                        }) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}