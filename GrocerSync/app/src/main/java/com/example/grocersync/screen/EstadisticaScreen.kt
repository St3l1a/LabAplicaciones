package com.example.grocersync.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grocersync.database.Item

data class ProductStat(
    val name: String,
    val quantity: Int,
    val emoji: String   // puedes asignar un emoji según categoría o dejar fijo
)

// 📊 Calcula el Top 10 a partir de los ítems reales
private fun computeTopProducts(items: List<Item>): List<ProductStat> {
    return items
        .groupBy { it.nombre }
        .map { (name, group) ->
            ProductStat(
                name = name,
                quantity = group.sumOf { it.cantidad },
                emoji = getEmojiForCategory(group.firstOrNull()?.categoria ?: "")
            )
        }
        .sortedByDescending { it.quantity }
        .take(10)
}

// 🎨 Asigna un emoji según la categoría (puedes personalizarlo)
private fun getEmojiForCategory(category: String): String {
    return when (category.lowercase()) {
        "lácteos" -> "🥛"
        "panadería" -> "🍞"
        "huevos" -> "🥚"
        "verduras" -> "🥦"
        "frutas" -> "🍎"
        "conservas" -> "🥫"
        "pastas" -> "🍝"
        "bebidas" -> "🥤"
        "limpieza" -> "🧼"
        else -> "🛒"
    }
}

@Composable
fun StatisticsScreen(items: List<Item>) {
    val topProducts = computeTopProducts(items)
    val maxQty = topProducts.maxOfOrNull { it.quantity }?.toFloat() ?: 1f

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFCBE8FF))
                .drawBehind {
                    // Mismas burbujas decorativas que en MainListScreen
                    val colors = listOf(
                        Color(0xFF90CAF9), Color(0xFFA5D6A7), Color(0xFFFFCC80),
                        Color(0xFFCE93D8), Color(0xFF80DEEA), Color(0xFFFFAB91), Color(0xFFAED581)
                    )
                    val bubbles = listOf(
                        Triple(size.width * 0.15f, size.height * 0.10f, 300f),
                        Triple(size.width * 0.85f, size.height * 0.20f, 350f),
                        Triple(size.width * 0.50f, size.height * 0.50f, 400f),
                        Triple(size.width * 0.10f, size.height * 0.75f, 300f),
                        Triple(size.width * 0.80f, size.height * 0.88f, 350f),
                        Triple(size.width * 0.40f, size.height * 0.25f, 250f),
                        Triple(size.width * 0.70f, size.height * 0.65f, 280f)
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
                    .padding(24.dp)
            ) {
                // Título
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFEB3B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Estadísticas",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Top 10 productos más comprados",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF455A64),
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    items.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay productos en tu lista.\n¡Agrega algunos para ver estadísticas!",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    topProducts.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay suficientes datos para mostrar estadísticas.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(topProducts) { index, product ->
                                ProductStatRow(
                                    rank = index + 1,
                                    product = product,
                                    maxQty = maxQty
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductStatRow(rank: Int, product: ProductStat, maxQty: Float) {
    val barColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFBF8970)
        else -> Color(0xFF64B5F6)
    }

    val badgeBg = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFBF8970)
        else -> Color(0xFFE3F2FD)
    }

    val badgeText = when (rank) {
        1, 2 -> Color.Black
        3 -> Color.White
        else -> Color(0xFF1565C0)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.75f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeText
                    )
                }

                Text(text = product.emoji, fontSize = 22.sp)

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${product.quantity}x",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFE3F2FD))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = product.quantity / maxQty)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(barColor)
                )
            }
        }
    }
}