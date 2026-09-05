package com.example.vfx

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.NexusGold
import com.example.ui.theme.NexusOrange
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visual effects layer dedicated to the manga cover in DetailsScreen.
 * Loaded from assets/infocover_vfx.korv and backed by libkorva_vfx.so.
 */
@Composable
fun InfoCoverVfxLayer(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var vfxConfig by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vfxConfig = KorvaVfxEngine.loadVfxAsset(context)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "infocover_vfx_transition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.width.coerceAtLeast(size.height) / 2f) * pulseScale

            // Glowing radiant ambient aura behind the cover
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NexusGold.copy(alpha = auraAlpha * 0.7f),
                        NexusOrange.copy(alpha = auraAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.25f
                ),
                center = center,
                radius = baseRadius * 1.25f
            )

            // Orbiting ambient light particles from infocover_vfx
            val particleCount = 6
            for (i in 0 until particleCount) {
                val angle = Math.toRadians((rotationAngle + i * (360f / particleCount)).toDouble())
                val orbitDist = baseRadius * 0.85f
                val px = center.x + (orbitDist * cos(angle)).toFloat()
                val py = center.y + (orbitDist * sin(angle)).toFloat()

                drawCircle(
                    color = if (i % 2 == 0) NexusGold.copy(alpha = auraAlpha) else NexusOrange.copy(alpha = auraAlpha),
                    radius = 4f * pulseScale,
                    center = Offset(px, py)
                )
            }
        }
    }
}
