package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.data.model.CouponHistory
import com.nimroddayan.couponmanager.ui.viewmodel.HistoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.HistoryViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouponHistoryScreen(
        couponId: Long,
        viewModelFactory: HistoryViewModelFactory,
) {
    val viewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
    viewModel.setCouponId(couponId)
    val history by viewModel.history.collectAsState()
    var showUndoConfirmationDialog by remember { mutableStateOf<CouponHistory?>(null) }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("History") },
                )
            }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(history) { operation ->
                HistoryItem(
                        operation = operation,
                        onUndo = { showUndoConfirmationDialog = it },
                )
            }
        }

        showUndoConfirmationDialog?.let { operation ->
            ConfirmationDialog(
                    onConfirm = {
                        viewModel.undo(operation)
                        showUndoConfirmationDialog = null
                    },
                    onDismiss = { showUndoConfirmationDialog = null },
                    title = "Undo Operation",
                    message = "Are you sure you want to undo this operation?"
            )
        }
    }
}

@Composable
fun HistoryItem(
        operation: CouponHistory,
        onUndo: (CouponHistory) -> Unit,
) {
    operation.couponState?.let { couponState ->
        val coupon = Json.decodeFromString<Coupon>(couponState)

        fun getHistoryIcon(action: String): ImageVector {
            return when (action) {
                "Coupon Used" -> Icons.Default.ShoppingCart
                "Coupon Created" -> Icons.Default.AddCircle
                "Coupon Archived" -> Icons.Default.Archive
                else -> Icons.Default.Edit
            }
        }

        fun getHistoryColor(action: String): Color {
            return when (action) {
                "Coupon Used" -> Color(0xFF6750A4) // Primary
                "Coupon Created" -> Color(0xFF4CAF50) // Green
                "Coupon Archived" -> Color(0xFF757575) // Grey
                else -> Color(0xFF625B71) // Secondary
            }
        }

        ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(2.dp),
                colors =
                        CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                        )
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = getHistoryColor(operation.action).copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        Icon(
                                imageVector = getHistoryIcon(operation.action),
                                contentDescription = null,
                                tint = getHistoryColor(operation.action),
                                modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(
                            text = operation.action,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                            text = coupon.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (operation.action == "Coupon Used") {
                        Text(
                                text = "Used: ₪${operation.changeSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                        )
                    } else if (operation.action != "Coupon Created" &&
                                    operation.action != "Coupon Archived"
                    ) {
                        Text(
                                text = operation.changeSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                            text =
                                    SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                                            .format(operation.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                    )
                }

                if (!operation.action.equals("Coupon Created", ignoreCase = true)) {
                    IconButton(onClick = { onUndo(operation) }) {
                        Icon(
                                Icons.Filled.Undo,
                                contentDescription = "Undo",
                                tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
