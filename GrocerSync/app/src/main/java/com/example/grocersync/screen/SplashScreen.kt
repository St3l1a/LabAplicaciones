package com.example.grocersync.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Fade‑in del texto (1 segundo) + espera (3.5s) + fade‑out (0.5s) = 5s total
    val textAlpha = remember { Animatable(0f) }
    val screenAlpha = remember { Animatable(1f) }  // para el fade‑out final

    LaunchedEffect(Unit) {
        // 1. Aparece el texto
        textAlpha.animateTo(1f, animationSpec = tween(1000))
        // 2. Se mantiene visible
        delay(3000)
        // 3. Toda la pantalla se desvanece
        screenAlpha.animateTo(0f, animationSpec = tween(500))
        // 4. Navega al login
        onSplashFinished()
    }

    // Movimiento vertical oscilante (sube y baja suavemente)
    val infiniteTransition = rememberInfiniteTransition()
// Movimiento vertical (sube 600 px en 1.2 s)
    val bubbleOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -1800f,           // sube mucho antes de reiniciar
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing), // 12 segundos → muy lento
            repeatMode = RepeatMode.Restart
        )
    )

    val bubbleOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,   // poco balanceo lateral
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)          // fade‑out global
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
                // Posiciones base (incluyendo algunas desde abajo)
                val basePositions = listOf(
                    Offset(size.width * 0.2f, size.height * 0.2f),
                    Offset(size.width * 0.8f, size.height * 0.25f),
                    Offset(size.width * 0.6f, size.height * 0.6f),
                    Offset(size.width * 0.2f, size.height * 0.8f),
                    Offset(size.width * 0.9f, size.height * 0.9f),
                    // Desde abajo (aparecen fuera de pantalla y suben)
                    Offset(size.width * 0.1f, size.height * 1.1f),
                    Offset(size.width * 0.5f, size.height * 1.05f),
                    Offset(size.width * 0.85f, size.height * 1.0f),
                    Offset(size.width * 0.35f, size.height * 0.95f)

                )


                basePositions.forEachIndexed { index, pos ->
                    val x = pos.x + (bubbleOffsetX * cos(index * 0.5)).toFloat()
                    val y = pos.y + (bubbleOffsetY * sin(index * 0.7)).toFloat()
                    drawCircle(
                        color = colors[index % colors.size].copy(alpha = 0.35f),
                        radius = 350f + (150 * index % 5),   // ← círculos más grandes
                        center = Offset(x, y)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Texto con su propio fade‑in (heredará también el fade‑out del padre)
        Box(
            modifier = Modifier
                .alpha(textAlpha.value)
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
    }
}