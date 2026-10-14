package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RecordingRed
import kotlin.random.Random

@Composable
fun LiveRecordingWaveform(
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    barCount: Int = 24,
    height: Dp = 36.dp,
    waveColor: Color = RecordingRed
) {
    val phaseAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        phaseAnim.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val barWidth = (totalWidth / barCount) * 0.55f
        val spacing = (totalWidth / barCount) * 0.45f
        val canvasHeight = size.height

        val primaryColor = waveColor
        val fadedColor = waveColor.copy(alpha = 0.4f)

        for (i in 0 until barCount) {
            val normalizedIndex = i.toFloat() / barCount
            // Bell curve dampening towards edges
            val bellFactor = kotlin.math.sin(normalizedIndex * Math.PI).toFloat()

            // Pseudo-random height modulated by real live amplitude
            val seed = (i * 17 + (phaseAnim.value * 10).toInt())
            val pseudoRandom = (kotlin.math.sin(seed.toDouble()).toFloat() + 1f) / 2f
            val baseHeight = (0.25f + 0.75f * (amplitude * 0.8f + pseudoRandom * 0.2f)) * bellFactor
            val barHeightPx = (baseHeight * canvasHeight).coerceIn(4.dp.toPx(), canvasHeight)

            val x = i * (barWidth + spacing)
            val y = (canvasHeight - barHeightPx) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, fadedColor),
                    startY = y,
                    endY = y + barHeightPx
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeightPx),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun StaticAudioWaveformPreview(
    modifier: Modifier = Modifier,
    recordingId: Long,
    height: Dp = 20.dp,
    barCount: Int = 30,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    val randomHeights = remember(recordingId) {
        val rng = Random(recordingId)
        List(barCount) { 0.2f + rng.nextFloat() * 0.8f }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val totalWidth = size.width
        val barWidth = (totalWidth / barCount) * 0.5f
        val spacing = (totalWidth / barCount) * 0.5f
        val canvasHeight = size.height

        for (i in 0 until barCount) {
            val factor = randomHeights[i]
            val barHeightPx = (factor * canvasHeight).coerceAtLeast(3.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = (canvasHeight - barHeightPx) / 2f

            drawRoundRect(
                color = tint.copy(alpha = 0.65f),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeightPx),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
