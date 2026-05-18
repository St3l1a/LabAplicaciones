package com.example.grocersync.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.grocersync.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Navega automáticamente al login tras 2 segundos
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

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
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título con el mismo estilo que el login
            Box(
                modifier = Modifier
                    .background(Color(0xFFFFEB3B))
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GrocerSync",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Iconos representativos
            Row(
                modifier = Modifier.fillMaxWidth(0.8f),
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
    }
}