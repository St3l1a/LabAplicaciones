package com.example.grocersync.screen

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.grocersync.R
import com.example.grocersync.database.Item
import com.example.grocersync.database.ListaRepository
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AddItemScreen(
    repository: ListaRepository,
    listId: Int = 1,
    onItemAdded: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var producto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val pendingUri = remember { mutableStateOf<Uri?>(null) }

    fun createImageUri(context: Context): Uri {
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()

        val file = File(imagesDir, "photo_${System.currentTimeMillis()}.jpg")

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = pendingUri.value
        } else {
            imageUri = null
        }
    }

    fun openCamera() {
        val uri = createImageUri(context)
        pendingUri.value = uri
        cameraLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openCamera()
    }

    Scaffold { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFCBE8FF))
                .drawBehind {
                    val colors = listOf(
                        Color(0xFF90CAF9),
                        Color(0xFFA5D6A7),
                        Color(0xFFFFCC80)
                    )

                    val bubbles = listOf(
                        Triple(size.width * 0.2f, size.height * 0.2f, 300f),
                        Triple(size.width * 0.8f, size.height * 0.3f, 300f),
                        Triple(size.width * 0.5f, size.height * 0.7f, 300f)
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
                verticalArrangement = Arrangement.Center,
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
                        text = "Add Item",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Imagen
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier.size(180.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.camara),
                            contentDescription = null,
                            modifier = Modifier.size(180.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abrir cámara")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Producto
                TextField(
                    value = producto,
                    onValueChange = { producto = it },
                    label = { Text("Producto") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Categoría
                TextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cantidad
                TextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cantidad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón guardar
                Button(
                    onClick = {
                        if (producto.isBlank() || categoria.isBlank() || cantidad.isBlank()) {
                            Log.e("ADD_ITEM", "Campos vacíos")
                            return@Button
                        }

                        scope.launch {
                            val newItem = Item(
                                nombre = producto,
                                categoria = categoria,
                                cantidad = cantidad.toInt(),
                                comprado = false,
                                listaId = listId
                            )

                            repository.insertItem(newItem)

                            Log.d("ADD_ITEM", "Item guardado: $newItem")
                            onItemAdded()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF690FC),
                        contentColor = Color.White
                    )
                ) {
                    Text("Añadir item")
                }
            }
        }
    }
}