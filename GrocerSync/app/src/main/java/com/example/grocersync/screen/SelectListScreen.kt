package com.example.grocersync.ui

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.grocersync.ui.theme.GrocerSyncTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement




@Composable
fun SelectListScreen(
    onBack: () -> Unit = {},
    onListSelected: (String) -> Unit,


) {

    val currentList = "My List"
    var showDialog by remember { mutableStateOf(false) }

    val otherLists = listOf("Family", "Roma Trip", "Fallas")

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    .padding(16.dp)
            ) {

                // 🔝 HEADER
                Row(
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
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -------- ACTUAL --------
                SectionDivider("Owned")

                Spacer(modifier = Modifier.height(12.dp))

                ListCard(currentList) {
                    onListSelected(currentList)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // MEMBERS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD1C4E9), RoundedCornerShape(20.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(20.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Members ")
                        Icon(Icons.Default.Face, contentDescription = null)
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF69F0AE), CircleShape)
                            .padding(6.dp)
                    ) {
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Añadir")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -------- OTHERS --------
                SectionDivider("Others")

                Spacer(modifier = Modifier.height(12.dp))

                otherLists.forEach { list ->
                    ListCard(list) {
                        onListSelected(list)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
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
fun SectionDivider(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = Color.Black)
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Divider(modifier = Modifier.weight(1f), color = Color.Black)
    }
}

@Composable
fun ListCard(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6C6E8)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))

    ) {
        Text(
            text = text,
            color = Color.Black // aquí cambias el color del texto
        )
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

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    GrocerSyncTheme {
        SelectListScreen(onBack = { /* opcional */ },
            onListSelected = { listName -> "Prueba"
            })
    }
}