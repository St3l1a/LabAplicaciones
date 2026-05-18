package com.example.grocersync.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.grocersync.R
import com.example.grocersync.database.AppDatabase
import com.example.grocersync.database.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onLoginSuccess: (Int) -> Unit,
    onNavigateToSign: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Instancia de Firestore
    val dbFirestore = remember { FirebaseFirestore.getInstance() }
    // Room seguirá usándose para almacenar localmente los datos del usuario
    val roomDb = remember { AppDatabase.getDatabase(context) }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFCBE8FF))
                .drawBehind {
                    // ... mismo fondo decorativo ...
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
                        Triple(size.width * 0.6f, size.height * 0.6f, 350f),
                        Triple(size.width * 0.2f, size.height * 0.8f, 350f),
                        Triple(size.width * 0.9f, size.height * 0.9f, 350f)
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
                // ... misma cabecera con título e imágenes ...
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 40.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEB3B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GrocerSync",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Image(painter = painterResource(id = R.drawable.comida), contentDescription = null, modifier = Modifier.size(80.dp))
                        Image(painter = painterResource(id = R.drawable.camiseta), contentDescription = null, modifier = Modifier.size(80.dp))
                        Image(painter = painterResource(id = R.drawable.escoba), contentDescription = null, modifier = Modifier.size(80.dp))
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Campo Email
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                    ) {
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Password
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                    ) {
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Mensaje de error
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Botón Login (MODIFICADO)
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Completá todos los campos"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                try {
                                    dbFirestore.collection("users")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            if (querySnapshot.isEmpty) {
                                                errorMessage = "Usuario no encontrado"
                                                isLoading = false
                                                return@addOnSuccessListener
                                            }
                                            val document = querySnapshot.documents[0]
                                            val storedPassword = document.getString("password") ?: ""
                                            val userId = document.getLong("id")?.toInt()
                                                ?: document.id.hashCode()
                                            val nombre = document.getString("nombre") ?: ""

                                            if (password == storedPassword) {
                                                scope.launch(Dispatchers.IO) {
                                                    val dao = roomDb.listaDao()
                                                    val usuario = Usuario(
                                                        id = userId,
                                                        email = email,
                                                        nombre = nombre,
                                                        password = password
                                                    )
                                                    dao.insertUsuario(usuario)
                                                    withContext(Dispatchers.Main) {
                                                        isLoading = false
                                                        onLoginSuccess(userId)
                                                    }
                                                }
                                            } else {
                                                errorMessage = "Contraseña incorrecta"
                                                isLoading = false
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            errorMessage = "Error de conexión: ${exception.message}"
                                            isLoading = false
                                        }
                                } catch (e: Exception) {
                                    errorMessage = "Error inesperado: ${e.message}"
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading   // deshabilitado mientras carga
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Iniciar sesión")
                        }
                    }

                    // Botón Crear cuenta
                    TextButton(
                        onClick = onNavigateToSign,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear cuenta")
                    }
                }
            }
        }
    }
}
