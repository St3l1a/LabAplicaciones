package com.example.grocersync.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.grocersync.R
import com.example.grocersync.database.AppDatabase
import com.example.grocersync.database.Usuario
import com.example.grocersync.ui.theme.GrocerSyncTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignScreen(
    onSignSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val roomDb = remember { AppDatabase.getDatabase(context) }
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
                        Color(0xFF90CAF9),
                        Color(0xFFA5D6A7),
                        Color(0xFFFFCC80),
                        Color(0xFFCE93D8),
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Título
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEB3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Campos
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Botón Registro (MODIFICADO)
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank() || name.isBlank()) {
                            errorMessage = "Completa todos los campos"
                            return@Button
                        }

                        scope.launch {
                            try {
                                // 1. Crear usuario en Firestore (documento autogenerado)
                                val userMap = hashMapOf(
                                    "name" to name,
                                    "email" to email,
                                    "password" to password
                                    // el campo "id" se agregará después
                                )

                                db.collection("users")
                                    .add(userMap)
                                    .addOnSuccessListener { documentReference ->
                                        val docId = documentReference.id
                                        // Generar ID numérico único (hash del docId)
                                        val numericId = docId.hashCode()

                                        // 2. Actualizar el documento con el campo "id"
                                        documentReference.update("id", numericId)

                                        // 3. Guardar usuario en Room (operación en corrutina de IO)
                                        scope.launch(Dispatchers.IO) {
                                            val usuario = Usuario(
                                                id = numericId,
                                                email = email,
                                                nombre = name,
                                                password = password // si tu entidad incluye este campo
                                            )
                                            val dao = roomDb.listaDao()
                                            dao.insertUsuario(usuario) // Asegúrate de usar OnConflictStrategy.REPLACE si es necesario

                                            withContext(Dispatchers.Main) {
                                                onSignSuccess(docId) // Navega al login
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        errorMessage = "Error creando usuario: ${e.message}"
                                    }
                            } catch (e: Exception) {
                                errorMessage = "Error inesperado: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear cuenta")
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignPreview() {
    GrocerSyncTheme {
        SignScreen(onSignSuccess = {})
    }
}