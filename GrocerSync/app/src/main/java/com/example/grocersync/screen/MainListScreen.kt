package com.example.grocersync.ui

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.grocersync.R
import com.example.grocersync.ui.theme.GrocerSyncTheme

data class Product(
    val name: String,
    val info: String,
    val color: Color
)

@Composable
fun MainListScreen(
    listName: String,
    onAddClick: () -> Unit,
    onStatsClick: () -> Unit
) {

    var search by remember { mutableStateOf("") }

    val products = listOf(
        Product("Leche entera 1L", "Juan • Hace 2 horas", Color(0xFFE6C6E8)),
        Product("6 Plátanos", "María • Hace 8 horas", Color(0xFFE6C6E8)),
        Product("200g ternera", "Ana • Hace 1 día", Color(0xFFE6C6E8))
    )



    Scaffold(
        modifier = Modifier.fillMaxSize(),

        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 📊 Botón estadísticas
                FloatingActionButton(
                    onClick = { onStatsClick() },
                    containerColor = Color(0xFFFFEB3B),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.graph),
                        contentDescription = "Estadísticas",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // ➕ Botón añadir (el que ya tenías)
                FloatingActionButton(
                    onClick = { onAddClick() },
                    containerColor = Color(0xFFE6C6E8),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {padding ->

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
                        text = listName,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Buscar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(15.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(products) { product ->
                        ProductCard(product)
                    }
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

        Image(
            painter = painterResource(id = R.drawable.camara),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }

}



@Preview(showBackground = true)
@Composable
fun MainListPreview() {
    GrocerSyncTheme {
        MainListScreen(
            listName = "Mi lista",
            onAddClick = {},
            onStatsClick = {}

        )
    }
}