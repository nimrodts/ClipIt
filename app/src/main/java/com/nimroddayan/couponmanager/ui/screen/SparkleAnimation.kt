package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SparkleAnimation(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8A2BE2), // A nice purple color
    particleCount: Int = 12,
    animationDuration: Int = 1200
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle_transition")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_angle"
    )

    // A value that goes from 0f to 1f and repeats
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_scale"
    )

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { }, // Consume clicks to prevent interaction with the form
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val center = this.center

            rotate(degrees = angle) {
                for (i in 0 until particleCount) {
                    val particleAngle = i * (2 * PI / particleCount)

                    // Particles move from center outwards based on scale
                    val movementRadius = size.minDimension / 2 * scale

                    val x = center.x + (movementRadius * cos(particleAngle)).toFloat()
                    val y = center.y + (movementRadius * sin(particleAngle)).toFloat()

                    // Particle size peaks in the middle of the animation using a sine wave
                    val particleRadius = (6.dp.toPx() * sin(scale * PI)).toFloat()

                    // Particles fade out over the course of the animation
                    val alpha = 1f - scale

                    drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = particleRadius,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
