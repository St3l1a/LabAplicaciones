package com.example.grocersync.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.grocersync.database.Lista
import com.example.grocersync.database.ListaRepository
import com.example.grocersync.database.ListaUsuarioCrossRef
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun AddListScreen(
    usuarioId: Int,
    repository: ListaRepository,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFCBE8FF))
                .drawBehind {
                    val colors = listOf(
                        Color(0xFF90CAF9), Color(0xFFA5D6A7),
                        Color(0xFFFFCC80), Color(0xFFCE93D8),
                        Color(0xFF80DEEA)
                    )
                    val bubbles = listOf(
                        Triple(size.width * 0.2f, size.height * 0.2f, 350f),
                        Triple(size.width * 0.8f, size.height * 0.25f, 350f),
                        Triple(size.width * 0.6f, size.height * 0.6f, 350f)
                    )
                    bubbles.forEachIndexed { i, (x, y, r) ->
                        drawCircle(
                            color = colors[i % colors.size].copy(alpha = 0.35f),
                            radius = r,
                            center = Offset(x, y)
                        )
                    }
                }
                .padding(padding)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEB3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Nueva lista",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la lista") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (nombre.isNotBlank()) {
                            scope.launch {
                                val nuevaLista = Lista(
                                    id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                                    nombre = nombre,
                                    fechaCreacion = SimpleDateFormat("dd/MM/yyyy").format(Date()),
                                    idCreador = usuarioId
                                )
                                repository.insertLista(nuevaLista)          // Room + Firestore
                                // Relacionar el usuario con la lista
                                repository.dao.insertCrossRef(
                                    ListaUsuarioCrossRef(nuevaLista.id, usuarioId)
                                )
                                onBack()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear lista")
                }
            }
        }
    }
}