package com.example.grocersync.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Product(
    val name: String,
    val info: String
)

@Composable
fun MainListScreen() {

    var search by remember { mutableStateOf("") }

    val products = listOf(
        Product("Leche entera 1L", "Juan • Hace 2 horas"),
        Product("6 Plátanos", "María • Hace 8 horas"),
        Product("200g ternera", "Ana • Hace 1 día")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = Color(0xFF69F0AE),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
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

                // 🟡 HEADER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEB3B))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My List",
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🔍 BUSCADOR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
                ) {
                    TextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Buscar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 🏷️ CATEGORÍAS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip("Todos", Color(0xFFFFEB3B))
                    CategoryChip("Fruta", Color(0xFFFFE0B2))
                    CategoryChip("Lácteos", Color(0xFFE0E0E0))
                    CategoryChip("Carne", Color(0xFFFFAB91))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 📦 LISTA
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        ProductCard(product)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(16.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text)
    }
}

@Composable
fun ProductCard(product: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE6C6E8), RoundedCornerShape(16.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(1.dp, Color.Black, CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(product.name)
                Text(product.info, style = MaterialTheme.typography.bodySmall)
            }
        }

        Icon(Icons.Default.AddCircle, contentDescription = null)
    }
}