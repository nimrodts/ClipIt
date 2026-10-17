package com.nimroddayan.clipit.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.ui.motion.Motion
import com.nimroddayan.clipit.ui.theme.extendedColors

/** Haptics with the meaning of each moment spelled out, so call sites stay consistent. */
class Haptics(private val view: View) {
    /** Crossing a commit line during a drag. */
    fun tick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /** A long press fired. */
    fun longPress() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /** An action completed: swipe committed, amount used, code copied. */
    fun confirm() {
        view.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.VIRTUAL_KEY
        )
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}

/** Parses a category's stored hex color, falling back to a neutral grey. */
fun categoryColor(hex: String?): Color =
        try {
            if (hex == null) Color(0xFF969D99) else Color(android.graphics.Color.parseColor(hex))
        } catch (e: IllegalArgumentException) {
            Color(0xFF969D99)
        }

@Composable
fun CategoryDot(color: Color, modifier: Modifier = Modifier, size: Dp = 8.dp) {
    Box(modifier.size(size).clip(CircleShape).background(color))
}

/** Uppercase caption above a group or list section. */
@Composable
fun SectionCaption(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
            modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.Bottom
    ) {
        Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                    trailing,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.extendedColors.inkTertiary
            )
        }
    }
}

/** An inset grouped list (iOS-style): rounded container with hairline separators between rows. */
@Composable
fun GroupedList(
        modifier: Modifier = Modifier,
        container: Color = MaterialTheme.extendedColors.card,
        content: @Composable ColumnScope.() -> Unit
) {
    Column(
            modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(container),
            content = content
    )
}

@Composable
fun GroupDivider() {
    HorizontalDivider(
            modifier = Modifier.padding(start = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.extendedColors.hairline
    )
}

/** One row in a [GroupedList]. Dims on press immediately, before release. */
@Composable
fun GroupRow(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressedColor = MaterialTheme.extendedColors.fill
    Row(
            modifier =
                    modifier.fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .then(
                                    if (onClick != null)
                                            Modifier.clickable(
                                                    interactionSource = interaction,
                                                    indication = null,
                                                    onClick = onClick
                                            )
                                    else Modifier
                            )
                            .background(if (pressed) pressedColor else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            content = content
    )
}

/** Scales the element down while pressed: feedback on touch-down, a spring back on release. */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.97f): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by
            animateFloatAsState(
                    targetValue = if (pressed) pressedScale else 1f,
                    animationSpec = if (pressed) tween(100, easing = LinearOutSlowInEasing) else Motion.standard(0.3f),
                    label = "pressScale"
            )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
