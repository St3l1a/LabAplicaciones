package com.example.grocersync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.grocersync.database.AppDatabase
import com.example.grocersync.database.Lista
import com.example.grocersync.database.ListaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectListScreen(
    usuarioId: Int,
    onListSelected: (Int) -> Unit,
    onBack: () -> Unit = {},
    onAddClick: () -> Unit,
    onStatsClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dao = remember { db.listaDao() }
    val repository = remember { ListaRepository(dao) }

    var showDialog by remember { mutableStateOf(false) }

    var listas by remember { mutableStateOf<List<Lista>>(emptyList()) }

    // 🔥 MAPA GLOBAL DE MIEMBROS (SOLUCIÓN REAL)
    var miembrosPorLista by remember { mutableStateOf<Map<Int, List<String>>>(emptyMap()) }

    // Cargar listas
    LaunchedEffect(usuarioId) {
        listas = repository.obtenerListasDeUsuario(dao, usuarioId)
    }

    // 🔥 Cargar TODOS los miembros UNA sola vez
    LaunchedEffect(listas) {
        val map = mutableMapOf<Int, List<String>>()

        listas.forEach { lista ->
            map[lista.id] = repository.obtenerMiembrosDeLista(dao, lista.id)
        }

        miembrosPorLista = map
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFCBE8FF))
                .drawBehind {

                    val colors = listOf(
                        Color(0xFF90CAF9),
                        Color(0xFFA5D6A7),
                        Color(0xFFFFCC80),
                        Color(0xFFCE93D8),
                        Color(0xFF80DEEA),
                        Color(0xFFFFAB91),
                        Color(0xFFAED581)
                    )

                    val bubbles = listOf(
                        Triple(size.width * 0.15f, size.height * 0.20f, 350f),
                        Triple(size.width * 0.80f, size.height * 0.18f, 350f),
                        Triple(size.width * 0.60f, size.height * 0.45f, 350f),
                        Triple(size.width * 0.20f, size.height * 0.70f, 350f),
                        Triple(size.width * 0.85f, size.height * 0.85f, 350f),
                        Triple(size.width * 0.50f, size.height * 0.10f, 350f),
                        Triple(size.width * 0.30f, size.height * 0.50f, 350f)
                    )

                    bubbles.forEachIndexed { i, (x, y, r) ->
                        drawCircle(
                            color = colors[i % colors.size].copy(alpha = 0.35f),
                            radius = r,
                            center = Offset(x, y)
                        )
                    }
                }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEB3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Lists",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Owned", style = MaterialTheme.typography.titleMedium)

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    items(listas) { lista ->

                        val miembros = miembrosPorLista[lista.id] ?: emptyList()

                        ListaCard(
                            nombre = lista.nombre,
                            fecha = lista.fechaCreacion,
                            miembros = miembros,
                            onClick = { onListSelected(lista.id) },
                            onAddClick = { showDialog = true }
                        )
                    }
                }
            }
        }
    }

    EmailDialog(
        showDialog = showDialog,
        onDismiss = { showDialog = false },
        onAccept = { email ->
            println("Email introducido: $email")
        }
    )
}

@Composable
fun ListaCard(
    nombre: String,
    fecha: String,
    miembros: List<String>,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val pastelColors = listOf(
        Color(0xFFFFC1CC),
        Color(0xFFFFE4B5),
        Color(0xFFFFF4B2),
        Color(0xFFC8E6C9),
        Color(0xFFB3E5FC),
        Color(0xFFD1C4E9),
        Color(0xFFFFD8B1),
        Color(0xFFE1F5FE),
        Color(0xFFF8BBD0),
        Color(0xFFDCEDC8)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(pastelColors.random(), RoundedCornerShape(20.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(20.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.padding(16.dp)) {
            Text(nombre)
            Text("Creada el $fecha", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            Text("Members:")

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                miembros.forEach { miembro ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Face, contentDescription = null)
                        Text(miembro)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .background(Color(0xFF69F0AE), CircleShape)
                .padding(6.dp)
        ) {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }
    }
}

@Composable
fun EmailDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAccept: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },

            title = {
                Text("Añadir miembro")
            },

            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("correo@email.com") },
                    singleLine = true
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        onAccept(email)
                        onDismiss()
                    }
                ) {
                    Text("Aceptar")
                }
            },

            dismissButton = {
                Button(
                    onClick = { onDismiss() }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}