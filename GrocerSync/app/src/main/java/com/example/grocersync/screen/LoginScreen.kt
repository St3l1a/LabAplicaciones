package com.example.grocersync.screen

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
import androidx.compose.ui.tooling.preview.Preview
import com.example.grocersync.R
import com.example.grocersync.database.AppDatabase
import com.example.grocersync.database.ListaRepository
import com.example.grocersync.ui.MainListScreen
import com.example.grocersync.ui.theme.GrocerSyncTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Instanciamos la base de datos (suficiente para un prototipo)
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { ListaRepository(db.listaDao()) }

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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 40.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // 🟡 TÍTULO
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

                    // 🖼️ IMÁGENES
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.comida),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.camiseta),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.escoba),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Column( modifier = Modifier .fillMaxWidth() .weight(1f), verticalArrangement = Arrangement.Top ){
                    // 📧 EMAIL
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(12.dp)
                            )
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

                    // 🔒 PASSWORD
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = Color.Black,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
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

                    // 🔘 LOGIN
                    // Mensaje de error
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Botón Login
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Completá todos los campos"
                                return@Button
                            }
                            scope.launch {
                                val usuario = withContext(Dispatchers.IO) {
                                    repository.login(email, password)
                                }
                                if (usuario != null) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "Email o contraseña incorrectos"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Iniciar sesión")
                    }

                    // Crear cuenta (opcional)
                    TextButton(
                        onClick = { /* Registrar usuario (próximo paso) */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear cuenta")
                    }
                }




            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    GrocerSyncTheme {
        LoginScreen(
            onLoginSuccess = {}
        )
    }
}