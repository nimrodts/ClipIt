package com.nimroddayan.clipit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.ai.AiStatus
import com.nimroddayan.clipit.data.model.Category
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.ui.theme.extendedColors
import com.nimroddayan.clipit.util.formatDate
import com.nimroddayan.clipit.util.formatMoneyShort

/** A coupon the AI found in an SMS, waiting to be added or dismissed. Tapping it opens the editor. */
@Composable
fun PendingCouponCard(
        coupon: Coupon,
        category: Category?,
        currencySymbol: String,
        isRetryingAi: Boolean,
        onAdd: () -> Unit,
        onDismiss: () -> Unit,
        onEdit: () -> Unit,
        onRetryAi: () -> Unit,
        modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.extendedColors
    val interaction = remember { MutableInteractionSource() }
    Column(
            modifier.pressScale(interaction, 0.98f)
                    .fillMaxWidth()
                    .clip(CouponCardShape)
                    .background(ext.card)
                    .clickable(interactionSource = interaction, indication = null, onClick = onEdit)
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AiBadge()
            Spacer(Modifier.width(8.dp))
            Text(
                    coupon.sourceSender?.let { "From $it" } ?: "From SMS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                    coupon.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(10.dp))
            SmallButton("Dismiss", filled = false, onClick = onDismiss)
            Spacer(Modifier.width(6.dp))
            SmallButton("Add", filled = true, onClick = onAdd)
        }
        val meta =
                listOfNotNull(
                        if (coupon.isOneTime) "Single use" else formatMoneyShort(coupon.currentValue, currencySymbol),
                        category?.name,
                        "until ${formatDate(coupon.expirationDate)}"
                )
        Text(meta.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val sms = coupon.sourceSms ?: coupon.creationMessage
        if (!sms.isNullOrBlank()) {
            Text(
                    sms,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                            Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ext.fill)
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        }
        if (coupon.aiStatus != null) {
            AiFailureRow(coupon, isRetryingAi, onRetryAi)
        }
    }
}

@Composable
private fun AiBadge() {
    Row(
            Modifier.height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.AutoAwesome, null, Modifier.size(11.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(4.dp))
        Text("AI", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun AiFailureRow(coupon: Coupon, isRetryingAi: Boolean, onRetryAi: () -> Unit) {
    val status =
            if (coupon.aiStatus == AiStatus.FAILED_RETRYING) "AI unavailable. Retrying automatically."
            else "AI couldn’t read this message."
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
                coupon.aiError?.let { "$status $it" } ?: status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        val interaction = remember { MutableInteractionSource() }
        Row(
                Modifier.pressScale(interaction, 0.95f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !isRetryingAi, interactionSource = interaction, indication = null, onClick = onRetryAi)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRetryingAi) {
                CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Outlined.Refresh, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(4.dp))
            Text("Retry with AI", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SmallButton(label: String, filled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ext = MaterialTheme.extendedColors
    val interaction = remember { MutableInteractionSource() }
    val bg: Color = if (filled) MaterialTheme.colorScheme.primary else ext.fillStrong
    val fg: Color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Row(
            modifier.pressScale(interaction, 0.95f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                    .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = fg)
    }
}
