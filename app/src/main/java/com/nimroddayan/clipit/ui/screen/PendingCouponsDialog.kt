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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.model.Coupon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingCouponsDialog(
        pendingCoupons: List<Coupon>,
        onApprove: (Coupon) -> Unit,
        onReject: (Coupon) -> Unit,
        onEdit: (Coupon) -> Unit,
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
                                    onEdit = { onEdit(coupon) }
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
        onEdit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onEdit() }.padding(vertical = 8.dp)) {
        Text(
                text = coupon.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
        )
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
