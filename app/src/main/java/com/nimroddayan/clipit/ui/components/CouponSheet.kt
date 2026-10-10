package com.nimroddayan.clipit.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.nimroddayan.clipit.data.model.Category
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.ui.motion.Motion
import com.nimroddayan.clipit.ui.theme.extendedColors
import com.nimroddayan.clipit.ui.theme.tabular
import com.nimroddayan.clipit.util.ExpiryUrgency
import com.nimroddayan.clipit.util.expiryLabel
import com.nimroddayan.clipit.util.formatDate
import com.nimroddayan.clipit.util.formatMoney
import com.nimroddayan.clipit.util.formatMoneyShort
import com.nimroddayan.clipit.util.parseAmount
import kotlinx.coroutines.delay

/** The header of the sheet: the part that is dragged to dismiss. */
@Composable
fun CouponSheetHeader(
        coupon: Coupon,
        category: Category?,
        currencySymbol: String,
        onDone: () -> Unit,
        modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val expiry = remember(coupon.expirationDate) { expiryLabel(coupon.expirationDate) }
    val balance by animateFloatAsState(coupon.currentValue.toFloat(), Motion.standard(0.5f), label = "sheetBalance")
    val fraction by
            animateFloatAsState(
                    if (coupon.initialValue > 0) (coupon.currentValue / coupon.initialValue).toFloat() else 0f,
                    Motion.standard(0.5f),
                    label = "sheetBar"
            )
    Column(modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 18.dp)) {
        Box(
                Modifier.align(Alignment.CenterHorizontally)
                        .padding(end = 8.dp)
                        .size(width = 38.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ext.fillStrong)
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            CategoryDot(categoryColor(category?.colorHex))
            Spacer(Modifier.width(8.dp))
            Text(
                    (category?.name ?: "Uncategorized") + if (coupon.isOneTime) " · Single use" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDone) { Text("Done", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
        }
        Column(Modifier.padding(end = 8.dp)) {
            Text(
                    coupon.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )
            if (!coupon.isOneTime) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(formatMoney(balance.toDouble(), currencySymbol), style = MaterialTheme.typography.displayLarge)
                    Text(
                            "of ${formatMoney(coupon.initialValue, currencySymbol)}",
                            style = MaterialTheme.typography.bodyLarge.tabular,
                            color = ext.inkTertiary,
                            modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                BalanceBar(fraction, dim = expiry.daysLeft < 0, height = 6)
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Text(expiry.text, style = MaterialTheme.typography.bodyMedium, color = expiryColor(expiry),
                        fontWeight = if (expiry.urgency == ExpiryUrgency.Normal) FontWeight.Normal else FontWeight.Medium)
                if (expiry.daysLeft >= 0) {
                    Text(
                            " · until ${formatDate(coupon.expirationDate)}",
                            style = MaterialTheme.typography.bodyMedium.tabular,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** The scrolling body of the sheet: code, use/redeem, history, message and management actions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CouponSheetBody(
        coupon: Coupon,
        currencySymbol: String,
        focusUse: Boolean,
        onUse: (Double) -> Unit,
        onRedeem: () -> Unit,
        onShowCode: (Offset) -> Unit,
        onHistory: () -> Unit,
        onEdit: () -> Unit,
        onArchive: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val haptics = rememberHaptics()
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val focusManager = LocalFocusManager.current

    Column(
            modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        val code = coupon.redeemCode?.takeIf { it.isNotBlank() }
        if (code != null) {
            var copied by remember { mutableStateOf(false) }
            LaunchedEffect(copied) {
                if (copied) {
                    delay(1600)
                    copied = false
                }
            }
            PressableBlock(
                    onClick = {
                        clipboard.setText(AnnotatedString(code))
                        copied = true
                        haptics.confirm()
                    }
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("REDEEM CODE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(code, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, letterSpacing = 0.05.em)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.ContentCopy, null, Modifier.size(15.dp),
                                tint = if (copied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                        Text(
                                if (copied) "Copied" else "Tap to copy",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (copied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (copied) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        val url = coupon.redemptionUrl?.takeIf { it.isNotBlank() }
        if (url != null) {
            PressableBlock(onClick = { uriHandler.openUri(url) }) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Open redemption page", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
        if (code != null || url != null) {
            var center by remember { mutableStateOf(Offset.Zero) }
            PressableBlock(
                    onClick = { onShowCode(center) },
                    modifier = Modifier.onGloballyPositioned { center = it.boundsInWindow().center }
            ) {
                Row(Modifier.fillMaxWidth().height(46.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.QrCode2, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Show QR & barcode", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
                }
            }
        }

        if (coupon.isOneTime) {
            PrimaryButton("Mark as redeemed", onClick = {
                haptics.confirm()
                onRedeem()
            })
        } else if (coupon.currentValue > 0) {
            UseAmountSection(coupon, currencySymbol, focusUse) { amount ->
                focusManager.clearFocus()
                haptics.confirm()
                onUse(amount)
            }
        }

        SectionCaption("Details", Modifier.padding(top = 6.dp))
        GroupedList(container = ext.fill) {
            GroupRow(onClick = onHistory) {
                Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("History", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ChevronRight, null, tint = ext.inkTertiary)
            }
            val message = coupon.sourceSms ?: coupon.creationMessage
            if (!message.isNullOrBlank()) {
                GroupDivider()
                var expanded by rememberSaveable(coupon.id) { mutableStateOf(false) }
                GroupRow(onClick = { expanded = !expanded }) {
                    Icon(Icons.Outlined.Sms, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Original message", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    coupon.sourceSender?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    }
                }
                AnimatedVisibility(expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                    Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 52.dp, end = 16.dp, bottom = 14.dp)
                    )
                }
            }
            GroupDivider()
            GroupRow(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Edit coupon", style = MaterialTheme.typography.bodyLarge)
            }
            GroupDivider()
            GroupRow(onClick = onArchive) {
                Icon(Icons.Outlined.Archive, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Archive", style = MaterialTheme.typography.bodyLarge)
            }
            GroupDivider()
            // Delete can't be undone, so it asks for a second tap instead of a dialog.
            var armed by remember { mutableStateOf(false) }
            LaunchedEffect(armed) {
                if (armed) {
                    delay(3000)
                    armed = false
                }
            }
            GroupRow(onClick = { if (armed) onDelete() else armed = true }) {
                Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error)
                Text(
                        if (armed) "Tap again to delete permanently" else "Delete",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UseAmountSection(
        coupon: Coupon,
        currencySymbol: String,
        focusUse: Boolean,
        onUse: (Double) -> Unit,
) {
    val ext = MaterialTheme.extendedColors
    var text by rememberSaveable(coupon.id) { mutableStateOf("") }
    val amount = parseAmount(text)
    val error =
            when {
                text.isBlank() -> null
                amount == null || amount <= 0 -> "Enter an amount above zero."
                amount > coupon.currentValue + 1e-9 ->
                        "That’s more than the ${formatMoney(coupon.currentValue, currencySymbol)} left on this card."
                else -> null
            }
    val valid = error == null && amount != null && amount > 0
    val focus = remember { FocusRequester() }
    LaunchedEffect(focusUse) {
        if (focusUse) {
            delay(380) // let the sheet arrive before the keyboard does
            focus.requestFocus()
        }
    }
    val submit = {
        if (valid && amount != null) {
            onUse(amount)
            text = ""
        }
    }
    Column(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(ext.fill).padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("AMOUNT TO USE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(currencySymbol, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            Box(Modifier.weight(1f)) {
                if (text.isEmpty()) {
                    Text("0.00", style = MaterialTheme.typography.displaySmall.tabular, color = ext.inkTertiary)
                }
                BasicTextField(
                        value = text,
                        onValueChange = { new -> text = new.filter { it.isDigit() || it == '.' || it == ',' }.take(10) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.displaySmall.tabular.copy(color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus)
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(if (error != null) MaterialTheme.colorScheme.error else ext.hairline))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(25.0, 50.0, 100.0).filter { it < coupon.currentValue }.forEach { q ->
                QuickChip(formatMoneyShort(q, currencySymbol)) { text = formatPlain(q) }
            }
            QuickChip("All ${formatMoney(coupon.currentValue, currencySymbol)}") { text = formatPlain(coupon.currentValue) }
        }
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        PrimaryButton(
                if (valid && amount != null) "Use ${formatMoney(amount, currencySymbol)}" else "Use amount",
                enabled = valid,
                onClick = submit
        )
    }
}

private fun formatPlain(value: Double): String =
        if (value % 1.0 == 0.0) value.toLong().toString() else String.format(java.util.Locale.US, "%.2f", value)

@Composable
private fun QuickChip(label: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
            Modifier.pressScale(interaction, 0.94f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.extendedColors.card)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                    .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium.tabular.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
fun PrimaryButton(label: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
            modifier.then(if (enabled) Modifier.pressScale(interaction) else Modifier)
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = if (enabled) 1f else 0.38f))
                    .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
    ) {
        Text(
                label,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall.tabular.copy(fontSize = 17.sp)
        )
    }
}

/** A filled block that shrinks slightly on touch-down. */
@Composable
fun PressableBlock(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.extendedColors.fill,
        content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
            modifier.pressScale(interaction, 0.98f)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(color)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) { content() }
}
