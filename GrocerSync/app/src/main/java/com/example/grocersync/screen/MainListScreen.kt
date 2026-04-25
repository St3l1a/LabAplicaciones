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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class Product(
    val name: String,
    val info: String,
    val color: Color
)

@Composable
fun MainListScreen() {

    var search by remember { mutableStateOf("") }

    val products = listOf(
        Product("Leche entera 1L", "Juan • Hace 2 horas", Color(0xFFE6C6E8)),
        Product("6 Plátanos", "María • Hace 8 horas", Color(0xFFE6C6E8)),
        Product("200g ternera", "Ana • Hace 1 día", Color(0xFFE6C6E8))
    )

    Scaffold(
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFEAF4FF))
                .padding(16.dp)
        ) {

            Text(
                text = "My List",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("Buscar") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(products) { product ->
                    ProductCard(product)
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(product.color, RoundedCornerShape(16.dp))
            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {
            Text(product.name)
            Text(product.info, style = MaterialTheme.typography.bodySmall)
        }

        Icon(Icons.Default.AddCircle, contentDescription = null)
    }
}