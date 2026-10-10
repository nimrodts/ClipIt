package com.nimroddayan.clipit.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.ai.AiStatus
import com.nimroddayan.clipit.data.model.Coupon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingCouponsDialog(
        pendingCoupons: List<Coupon>,
        onApprove: (Coupon) -> Unit,
        onReject: (Coupon) -> Unit,
        onEdit: (Coupon) -> Unit,
        onRetryAi: (Coupon) -> Unit,
        aiRetryingIds: Set<Long>,
        onDismiss: () -> Unit
) {
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Pending Coupons") },
            text = {
                if (pendingCoupons.isEmpty()) {
                    Text("No pending coupons found.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        items(pendingCoupons) { coupon ->
                            PendingCouponItem(
                                    coupon = coupon,
                                    onApprove = { onApprove(coupon) },
                                    onReject = { onReject(coupon) },
                                    onEdit = { onEdit(coupon) },
                                    onRetryAi = { onRetryAi(coupon) },
                                    isRetryingAi = coupon.id in aiRetryingIds
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun PendingCouponItem(
        coupon: Coupon,
        onApprove: () -> Unit,
        onReject: () -> Unit,
        onEdit: () -> Unit,
        onRetryAi: () -> Unit,
        isRetryingAi: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onEdit() }.padding(vertical = 8.dp)) {
        Text(
                text = coupon.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
        )
        if (coupon.aiStatus != null) {
            AiFailureRow(coupon = coupon, onRetryAi = onRetryAi, isRetryingAi = isRetryingAi)
        }
        Text(text = "Value: ${coupon.currentValue}", style = MaterialTheme.typography.bodyMedium)
        if (!coupon.redeemCode.isNullOrBlank()) {
            Text(
                    text = "Code: ${coupon.redeemCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
            )
        } else if (!coupon.redemptionUrl.isNullOrBlank()) {
            Row(
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                        text = "Link Available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                    onClick = onReject,
                    colors =
                            ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                            )
            ) {
                Icon(Icons.Default.Close, contentDescription = "Reject")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reject")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onApprove) {
                Icon(Icons.Default.Check, contentDescription = "Approve")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Approve")
            }
        }
    }
}

@Composable
private fun AiFailureRow(coupon: Coupon, onRetryAi: () -> Unit, isRetryingAi: Boolean) {
    val status =
            if (coupon.aiStatus == AiStatus.FAILED_RETRYING) "AI unavailable — retrying automatically"
            else "AI failed"
    Text(
            text = coupon.aiError?.let { "$status: $it" } ?: status,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
    )
    TextButton(onClick = onRetryAi, enabled = !isRetryingAi) {
        if (isRetryingAi) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text("Retry with AI")
    }
}
