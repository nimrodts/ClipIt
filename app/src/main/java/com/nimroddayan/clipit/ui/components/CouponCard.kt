package com.nimroddayan.clipit.ui.components

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nimroddayan.clipit.data.model.Category
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.ui.motion.Motion
import com.nimroddayan.clipit.ui.motion.rubberband
import com.nimroddayan.clipit.ui.motion.shouldCommitSwipe
import com.nimroddayan.clipit.ui.theme.extendedColors
import com.nimroddayan.clipit.ui.theme.tabular
import com.nimroddayan.clipit.util.ExpiryLabel
import com.nimroddayan.clipit.util.ExpiryUrgency
import com.nimroddayan.clipit.util.expiryLabel
import com.nimroddayan.clipit.util.formatMoney
import com.nimroddayan.clipit.util.formatMoneyShort
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val CouponCardShape = RoundedCornerShape(18.dp)

/** Fraction of the card width a swipe must (be projected to) travel to commit. */
private const val CommitFraction = 0.32f
/** Past this fraction of the width the card resists instead of following 1:1. */
private const val ResistFraction = 0.55f
private const val LongPressMillis = 450L
private const val HoldHintMillis = 120L

@Composable
fun expiryColor(expiry: ExpiryLabel): Color =
        when (expiry.urgency) {
            ExpiryUrgency.Expired -> MaterialTheme.colorScheme.error
            ExpiryUrgency.Soon -> MaterialTheme.extendedColors.warn
            ExpiryUrgency.Normal -> MaterialTheme.colorScheme.onSurfaceVariant
        }

@Composable
fun BalanceBar(fraction: Float, dim: Boolean, modifier: Modifier = Modifier, height: Int = 4) {
    val ext = MaterialTheme.extendedColors
    Box(
            modifier.fillMaxWidth()
                    .height(height.dp)
                    .clip(RoundedCornerShape((height / 2).dp))
                    .background(ext.fillStrong)
    ) {
        Box(
                Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape((height / 2).dp))
                        .background(if (dim) ext.inkTertiary else MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun Pill(text: String, modifier: Modifier = Modifier) {
    Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraSmall,
            color = MaterialTheme.extendedColors.fill,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Text(
                text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** The card's face: name and balance, category and relative expiry, balance bar. */
@Composable
fun CouponCardContent(
        coupon: Coupon,
        category: Category?,
        currencySymbol: String,
        modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val expiry = remember(coupon.expirationDate) { expiryLabel(coupon.expirationDate) }
    val fraction by
            animateFloatAsState(
                    targetValue =
                            if (coupon.initialValue > 0) (coupon.currentValue / coupon.initialValue).toFloat()
                            else 0f,
                    animationSpec = Motion.standard(0.5f),
                    label = "balance"
            )
    Column(modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    coupon.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            if (coupon.isOneTime) {
                Pill("Single use")
            } else {
                Text(
                        formatMoney(coupon.currentValue, currencySymbol),
                        style =
                                MaterialTheme.typography.titleLarge
                                        .copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                        .tabular,
                        color =
                                if (coupon.currentValue <= 0) ext.inkTertiary
                                else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CategoryDot(categoryColor(category?.colorHex))
            Text(
                    category?.name ?: "Uncategorized",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
            )
            Text("·", style = MaterialTheme.typography.bodySmall, color = ext.inkTertiary)
            Text(
                    expiry.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = expiryColor(expiry),
                    fontWeight = if (expiry.urgency == ExpiryUrgency.Normal) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 1
            )
            if (!coupon.isOneTime) {
                Spacer(Modifier.weight(1f))
                Text(
                        "of ${formatMoneyShort(coupon.initialValue, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall.tabular,
                        color = ext.inkTertiary,
                        maxLines = 1
                )
            }
        }
        if (!coupon.isOneTime) {
            Spacer(Modifier.height(12.dp))
            BalanceBar(fraction, dim = expiry.urgency == ExpiryUrgency.Expired && expiry.daysLeft < 0)
        }
    }
}

private enum class GestureMode { Pending, Swipe, Scroll, LongPress }

/**
 * A coupon card that is tapped to open, long-pressed to show its code, and swiped:
 * right to use (or redeem a single-use coupon), left to archive.
 *
 * The card follows the finger 1:1 after a touch-slop of hysteresis, resists past [ResistFraction]
 * of its width, and on release decides by projected position rather than where it was let go.
 * It can be caught mid-flight.
 *
 * @param cardModifier applied to the moving card surface (e.g. a shared-element modifier).
 */
@Composable
fun SwipeableCouponCard(
        coupon: Coupon,
        category: Category?,
        currencySymbol: String,
        onClick: () -> Unit,
        onLongPress: () -> Unit,
        onSwipeUse: () -> Unit,
        onSwipeRedeem: () -> Unit,
        onSwipeArchive: () -> Unit,
        modifier: Modifier = Modifier,
        cardModifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val haptics = rememberHaptics()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val offset = remember { Animatable(0f) }
    var widthPx by remember { mutableFloatStateOf(1f) }
    var pressed by remember { mutableStateOf(false) }
    var holding by remember { mutableStateOf(false) }
    var armed by remember { mutableStateOf(false) }

    val currentCoupon by rememberUpdatedState(coupon)
    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongPress by rememberUpdatedState(onLongPress)
    val currentOnUse by rememberUpdatedState(onSwipeUse)
    val currentOnRedeem by rememberUpdatedState(onSwipeRedeem)
    val currentOnArchive by rememberUpdatedState(onSwipeArchive)

    val scale by
            animateFloatAsState(
                    targetValue = if (holding) 0.95f else if (pressed) 0.97f else 1f,
                    animationSpec =
                            when {
                                holding -> tween(330, easing = FastOutLinearInEasing)
                                pressed -> tween(100, easing = LinearOutSlowInEasing)
                                else -> Motion.standard(0.3f)
                            },
                    label = "cardPress"
            )
    val iconScale by
            animateFloatAsState(
                    targetValue = if (armed) 1.28f else 1f,
                    animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
                    label = "armedIcon"
            )

    fun settle(velocity: Float) {
        val x = offset.value
        armed = false
        if (shouldCommitSwipe(x, velocity, widthPx * CommitFraction)) {
            haptics.confirm()
            val c = currentCoupon
            if (x < 0 || c.isOneTime) {
                scope.launch {
                    offset.animateTo(sign(x) * widthPx * 1.15f, Motion.standard(0.3f), velocity)
                    if (x < 0) currentOnArchive() else currentOnRedeem()
                }
            } else {
                scope.launch { offset.animateTo(0f, Motion.momentum(), velocity) }
                currentOnUse()
            }
        } else {
            scope.launch { offset.animateTo(0f, Motion.momentum(), velocity) }
        }
    }

    Box(modifier.fillMaxWidth().onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f) }) {
        val x = offset.value
        if (x != 0f) {
            val toRight = x > 0
            val revealAlpha = (abs(x) / with(density) { 40.dp.toPx() }).coerceIn(0f, 1f)
            Row(
                    modifier =
                            Modifier.matchParentSize()
                                    .graphicsLayer { alpha = revealAlpha }
                                    .clip(CouponCardShape)
                                    .background(if (toRight) MaterialTheme.colorScheme.primary else ext.archive)
                                    .padding(horizontal = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (toRight) Arrangement.Start else Arrangement.End
            ) {
                val tint = if (toRight) MaterialTheme.colorScheme.onPrimary else Color.White
                if (toRight) {
                    Icon(
                            if (coupon.isOneTime) Icons.Outlined.CheckCircle else Icons.Outlined.ShoppingCart,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (coupon.isOneTime) "Redeem" else "Use", color = tint, style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("Archive", color = tint, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                            Icons.Outlined.Archive,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.graphicsLayer { scaleX = iconScale; scaleY = iconScale }
                    )
                }
            }
        }

        Box(
                Modifier.graphicsLayer {
                            translationX = offset.value
                            scaleX = scale
                            scaleY = scale
                        }
                        .then(cardModifier)
                        .clip(CouponCardShape)
                        .background(ext.card)
                        .semantics(mergeDescendants = true) {
                            role = Role.Button
                            onClick(label = "Open details") {
                                currentOnClick()
                                true
                            }
                            onLongClick(label = "Show redeem code") {
                                currentOnLongPress()
                                true
                            }
                            customActions =
                                    listOf(
                                            CustomAccessibilityAction(
                                                    if (coupon.isOneTime) "Redeem" else "Use"
                                            ) {
                                                if (currentCoupon.isOneTime) currentOnRedeem() else currentOnUse()
                                                true
                                            },
                                            CustomAccessibilityAction("Archive") {
                                                currentOnArchive()
                                                true
                                            },
                                    )
                        }
                        .pointerInput(coupon.id) {
                            val slop = viewConfiguration.touchSlop
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val tracker = VelocityTracker()
                                tracker.addPosition(down.uptimeMillis, down.position)
                                val startOffset = offset.value
                                var mode = GestureMode.Pending
                                if (abs(startOffset) > 1f || offset.isRunning) {
                                    // Caught mid-flight: keep tracking from where it is.
                                    mode = GestureMode.Swipe
                                    scope.launch { offset.stop() }
                                } else {
                                    pressed = true
                                }
                                armed = abs(startOffset) > widthPx * CommitFraction
                                val holdJob =
                                        scope.launch {
                                            delay(HoldHintMillis)
                                            if (mode == GestureMode.Pending) holding = true
                                        }
                                val deadline = down.uptimeMillis + LongPressMillis
                                var hysteresis = 0f
                                var tapped = false
                                var releaseVelocity = 0f

                                while (true) {
                                    val event: PointerEvent? =
                                            if (mode == GestureMode.Pending) {
                                                val remaining = deadline - SystemClock.uptimeMillis()
                                                if (remaining <= 0) null
                                                else withTimeoutOrNull(remaining) { awaitPointerEvent() }
                                            } else {
                                                awaitPointerEvent()
                                            }

                                    if (event == null) {
                                        mode = GestureMode.LongPress
                                        pressed = false
                                        holding = false
                                        haptics.longPress()
                                        currentOnLongPress()
                                        // Swallow the rest of this touch so lifting doesn't count as a tap.
                                        do {
                                            val e = awaitPointerEvent()
                                            e.changes.forEach { it.consume() }
                                        } while (e.changes.any { it.pressed })
                                        break
                                    }

                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (change.changedToUpIgnoreConsumed()) {
                                        if (mode == GestureMode.Pending && !change.isConsumed) tapped = true
                                        if (mode == GestureMode.Swipe) {
                                            tracker.addPosition(change.uptimeMillis, change.position)
                                            releaseVelocity = tracker.calculateVelocity().x
                                        }
                                        break
                                    }
                                    if (mode == GestureMode.Pending && change.isConsumed) {
                                        mode = GestureMode.Scroll // a scrolling parent took this touch
                                        break
                                    }

                                    tracker.addPosition(change.uptimeMillis, change.position)
                                    val total: Offset = change.position - down.position
                                    if (mode == GestureMode.Pending) {
                                        if (abs(total.x) > slop && abs(total.x) > abs(total.y)) {
                                            mode = GestureMode.Swipe
                                            hysteresis = sign(total.x) * slop
                                            pressed = false
                                            holding = false
                                        } else if (abs(total.y) > slop) {
                                            mode = GestureMode.Scroll
                                            break
                                        }
                                    }
                                    if (mode == GestureMode.Swipe) {
                                        change.consume()
                                        val raw = startOffset + total.x - hysteresis
                                        val limit = widthPx * ResistFraction
                                        val next =
                                                if (abs(raw) > limit)
                                                        sign(raw) * (limit + rubberband(abs(raw) - limit, widthPx))
                                                else raw
                                        scope.launch { offset.snapTo(next) }
                                        val nowArmed = abs(next) > widthPx * CommitFraction
                                        if (nowArmed != armed) {
                                            armed = nowArmed
                                            haptics.tick()
                                        }
                                    }
                                }

                                holdJob.cancel()
                                pressed = false
                                holding = false
                                when {
                                    tapped -> currentOnClick()
                                    mode == GestureMode.Swipe -> settle(releaseVelocity)
                                }
                            }
                        }
        ) {
            CouponCardContent(coupon, category, currencySymbol)
        }
    }
}
