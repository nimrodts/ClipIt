package com.nimroddayan.clipit.ui.components

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.nimroddayan.clipit.data.model.Category
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.ui.motion.Motion
import com.nimroddayan.clipit.ui.theme.Accent
import com.nimroddayan.clipit.ui.theme.extendedColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** What a coupon can be redeemed with: its code if it has one, otherwise its link. */
fun Coupon.redeemValue(): String? =
        redeemCode?.takeIf { it.isNotBlank() } ?: redemptionUrl?.takeIf { it.isNotBlank() }

/**
 * The scannable redeem pass: QR code, CODE_128 barcode (for codes short enough to stay readable)
 * and the code as text. Screen brightness is raised while it is open. The pass stays light in both
 * themes because scanners need dark bars on a light ground.
 *
 * It grows out of [originInWindow] (the center of the card that was pressed) and returns to it.
 */
@Composable
fun RedeemPass(
        coupon: Coupon,
        category: Category?,
        originInWindow: Offset?,
        onDismiss: () -> Unit,
) {
    val value = coupon.redeemValue() ?: return
    val isLink = coupon.redeemCode.isNullOrBlank()
    val context = LocalContext.current

    // Raise brightness for scanning; restore on close. Uses the activity window, so it lives outside the Dialog.
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val original = activity?.window?.attributes?.screenBrightness
        activity?.window?.let { w ->
            w.attributes = w.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL }
        }
        onDispose {
            activity?.window?.let { w ->
                w.attributes = w.attributes.apply { screenBrightness = original ?: -1f }
            }
        }
    }

    val sanitized = remember(value) { value.filter { it.isLetterOrDigit() || it in ":/._-?=&%#+" } }
    val qr by
            produceState<Bitmap?>(null, sanitized) {
                this.value = withContext(Dispatchers.Default) { encode(sanitized, BarcodeFormat.QR_CODE, 480, 480) }
            }
    val barcode by
            produceState<Bitmap?>(null, sanitized) {
                this.value =
                        if (!isLink && sanitized.length <= 40)
                                withContext(Dispatchers.Default) { encode(sanitized, BarcodeFormat.CODE_128, 720, 160) }
                        else null
            }

    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var center by remember { mutableStateOf<Offset?>(null) }
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { progress.animateTo(1f, Motion.momentum(0.34f)) }
    fun close() {
        if (closing) return
        closing = true
        scope.launch {
            progress.animateTo(0f, Motion.standard(0.3f))
            onDismiss()
        }
    }

    Dialog(
            onDismissRequest = ::close,
            properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
                Modifier.fillMaxSize()
                        .onGloballyPositioned { center = it.boundsInWindow().center }
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = ::close)
                        .systemBarsPadding()
                        .padding(20.dp),
                contentAlignment = Alignment.Center
        ) {
            val ext = MaterialTheme.extendedColors
            val clipboard = LocalClipboardManager.current
            val uriHandler = LocalUriHandler.current
            val haptics = rememberHaptics()
            var copied by remember { mutableStateOf(false) }
            Column(
                    modifier =
                            Modifier.widthIn(max = 420.dp)
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        val p = progress.value
                                        val from = originInWindow
                                        val to = center
                                        if (from != null && to != null) {
                                            translationX = (from.x - to.x) * (1f - p)
                                            translationY = (from.y - to.y) * (1f - p)
                                        }
                                        val s = 0.5f + 0.5f * p
                                        scaleX = s
                                        scaleY = s
                                        alpha = (p * 1.6f).coerceIn(0f, 1f)
                                    }
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(ext.pass)
                                    // Taps on the pass itself don't dismiss.
                                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                                    .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CategoryDot(categoryColor(category?.colorHex))
                    Spacer(Modifier.width(8.dp))
                    Text(
                            category?.name ?: "Uncategorized",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.passInkSecondary,
                            modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = ::close) {
                        Text("Done", color = Accent, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                        coupon.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 21.sp),
                        color = ext.passInk,
                        modifier = Modifier.fillMaxWidth()
                )
                ScanImage(qr, contentDescription = "QR code", modifier = Modifier.fillMaxWidth(0.62f).aspectRatio(1f))
                if (barcode != null || (!isLink && sanitized.length <= 40)) {
                    ScanImage(barcode, contentDescription = "Barcode", modifier = Modifier.fillMaxWidth().height(64.dp), scale = ContentScale.FillBounds)
                }
                Text(
                        if (isLink) value.removePrefix("https://").removePrefix("http://") else value,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = if (isLink) 15.sp else 19.sp,
                        letterSpacing = if (isLink) 0.em else 0.08.em,
                        color = ext.passInk,
                        textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PassButton(
                            label = if (copied) "Copied" else if (isLink) "Copy link" else "Copy code",
                            icon = { Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp)) },
                            highlighted = copied,
                            modifier = Modifier.weight(1f)
                    ) {
                        clipboard.setText(AnnotatedString(value))
                        copied = true
                        haptics.confirm()
                    }
                    if (!coupon.redemptionUrl.isNullOrBlank()) {
                        PassButton(
                                label = "Open link",
                                icon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, Modifier.size(18.dp)) },
                                modifier = Modifier.weight(1f)
                        ) { uriHandler.openUri(coupon.redemptionUrl) }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.LightMode, null, tint = ext.passInkSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                            "Screen brightness is turned up while this is open",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.passInkSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PassButton(
        label: String,
        icon: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        highlighted: Boolean = false,
        onClick: () -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    val interaction = remember { MutableInteractionSource() }
    Row(
            modifier =
                    modifier.pressScale(interaction)
                            .height(46.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(ext.passFill)
                            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
    ) {
        val color = if (highlighted) Accent else ext.passInk
        CompositionLocalProvider(LocalContentColor provides color) {
            icon()
            Spacer(Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
private fun ScanImage(
        bitmap: Bitmap?,
        contentDescription: String,
        modifier: Modifier,
        scale: ContentScale = ContentScale.Fit,
) {
    if (bitmap == null) {
        Box(modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.extendedColors.passFill))
    } else {
        val image = remember(bitmap) { bitmap.asImageBitmap() }
        Image(image, contentDescription = contentDescription, modifier = modifier, contentScale = scale)
    }
}

private fun encode(content: String, format: BarcodeFormat, width: Int, height: Int): Bitmap? =
        try {
            val matrix = MultiFormatWriter().encode(content, format, width, height)
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val row = y * width
                for (x in 0 until width) {
                    pixels[row + x] = if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                }
            }
            Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            null
        }
