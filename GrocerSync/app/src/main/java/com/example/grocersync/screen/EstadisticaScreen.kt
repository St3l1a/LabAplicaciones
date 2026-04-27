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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grocersync.ui.theme.GrocerSyncTheme

data class ProductStat(
    val name: String,
    val quantity: Int,
    val emoji: String
)

val mockTopProducts = listOf(
    ProductStat("Leche entera", 24, "🥛"),
    ProductStat("Pan de molde", 18, "🍞"),
    ProductStat("Huevos (docena)", 15, "🥚"),
    ProductStat("Tomate frito", 13, "🍅"),
    ProductStat("Pasta espagueti", 11, "🍝"),
    ProductStat("Zumo de naranja", 10, "🍊"),
    ProductStat("Yogur natural", 9, "🫙"),
    ProductStat("Detergente ropa", 8, "🧺"),
    ProductStat("Arroz largo", 7, "🍚"),
    ProductStat("Papel higiénico", 6, "🧻")
)

val rankColors = listOf(
    Color(0xFFFFD700), // 🥇 Oro
    Color(0xFFB0BEC5), // 🥈 Plata
    Color(0xFFBF8970), // 🥉 Bronce
)

@Composable
fun StatisticsScreen() {

    val maxQty = mockTopProducts.maxOf { it.quantity }.toFloat()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->

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
                    .padding(24.dp)
            ) {

                // 🟡 TÍTULO
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

                // 📋 LISTA
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(mockTopProducts) { index, product ->
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
        1 -> Color.Black
        2 -> Color.Black
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
                // Badge de posición
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

                // Emoji + Nombre
                Text(text = product.emoji, fontSize = 22.sp)

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Cantidad
                Text(
                    text = "${product.quantity}x",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Barra de progreso
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StatisticsPreview() {
    GrocerSyncTheme {
        StatisticsScreen()
    }
}