package com.example.grocersync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.grocersync.data.AppDatabase
import kotlinx.coroutines.launch



@Composable
fun MainScreen(db: AppDatabase) {

    val dao = db.shoppingDao()
    val scope = rememberCoroutineScope()

    var lists by remember { mutableStateOf<List<ShoppingList>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    // Cargar listas
    LaunchedEffect(Unit) {
        lists = dao.getAllLists()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Mis listas 🛒",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(lists) { list ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(list.name)

                            Button(
                                onClick = {
                                    scope.launch {
                                        dao.deleteList(list)
                                        lists = dao.getAllLists()
                                    }
                                }
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog crear lista
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        if (newListName.isNotBlank()) {
                            dao.insertList(ShoppingList(name = newListName))
                            lists = dao.getAllLists()
                            newListName = ""
                            showDialog = false
                        }
                    }
                }) {
                    Text("Crear")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Nueva lista") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("Nombre") }
                )
            }
        )
    }
}