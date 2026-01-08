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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp

@Composable
fun SparkleAnimation(
        modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkle_transition")

    // Omni-present background scrim
    Box(
            modifier =
                    modifier.background(Color.Black.copy(alpha = 0.5f)).clickable(enabled = true) {
                    }, // Consume clicks
            contentAlignment = Alignment.Center
    ) {
        // Main rotating star
        val rotation1 by
                infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(4000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                ),
                        label = "rotation1"
                )
        val scale1 by
                infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(1500, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                ),
                        label = "scale1"
                )

        GeminiStar(
                modifier = Modifier.size(80.dp),
                rotation = rotation1,
                scale = scale1,
                colorList =
                        listOf(
                                Color(0xFF4285F4), // Google Blue
                                Color(0xFF9B72CB), // Purple
                                Color(0xFFD96570) // Red-ish
                        )
        )

        // Secondary star (smaller, faster rotation, different phase)
        val rotation2 by
                infiniteTransition.animateFloat(
                        initialValue = 360f,
                        targetValue = 0f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(3000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                ),
                        label = "rotation2"
                )
        val scale2 by
                infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 0.9f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(1200, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                ),
                        label = "scale2"
                )

        GeminiStar(
                modifier =
                        Modifier.size(40.dp)
                                .align(Alignment.Center)
                                .offset(30.dp, (-30).dp), // Offset from center
                rotation = rotation2,
                scale = scale2,
                colorList = listOf(Color(0xFFD96570), Color(0xFF4285F4))
        )

        // Third star
        val rotation3 by
                infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(5000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                ),
                        label = "rotation3"
                )
        val scale3 by
                infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 0.3f,
                        animationSpec =
                                infiniteRepeatable(
                                        animation = tween(2000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                ),
                        label = "scale3"
                )

        GeminiStar(
                modifier = Modifier.size(30.dp).align(Alignment.Center).offset((-25).dp, 35.dp),
                rotation = rotation3,
                scale = scale3,
                colorList = listOf(Color(0xFF9B72CB), Color(0xFF4285F4))
        )
    }
}

@Composable
fun GeminiStar(modifier: Modifier, rotation: Float, scale: Float, colorList: List<Color>) {
    Canvas(modifier = modifier) {
        val center = this.center
        val path = createStarPath(size.width, size.height)
        val brush = Brush.linearGradient(colors = colorList)

        rotate(rotation, center) { scale(scale, center) { drawPath(path, brush) } }
    }
}

fun createStarPath(width: Float, height: Float): Path {
    val path = Path()
    val centerX = width / 2
    val centerY = height / 2
    val halfWidth = width / 2
    val halfHeight = height / 2

    // Customize these factors to make the star thinner or thicker
    // For a Gemini-like star, the control points are closer to the center
    val innerFactor = 0.25f

    path.moveTo(centerX, 0f) // Top tip

    // Top-Right curve
    path.quadraticBezierTo(
            centerX + halfWidth * innerFactor,
            centerY - halfHeight * innerFactor,
            width,
            centerY
    )

    // Bottom-Right curve
    path.quadraticBezierTo(
            centerX + halfWidth * innerFactor,
            centerY + halfHeight * innerFactor,
            centerX,
            height
    )

    // Bottom-Left curve
    path.quadraticBezierTo(
            centerX - halfWidth * innerFactor,
            centerY + halfHeight * innerFactor,
            0f,
            centerY
    )

    // Top-Left curve
    path.quadraticBezierTo(
            centerX - halfWidth * innerFactor,
            centerY - halfHeight * innerFactor,
            centerX,
            0f
    )

    path.close()
    return path
}
