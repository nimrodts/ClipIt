package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.data.model.CouponHistory
import com.nimroddayan.couponmanager.ui.viewmodel.HistoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.HistoryViewModelFactory
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale

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
    val coupon = Json.decodeFromString<Coupon>(operation.couponState)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Coupon: ${coupon.name}")
                coupon.redeemCode?.takeIf { it.isNotBlank() }?.let {
                    Text("Redeem Code: $it", fontSize = 12.sp)
                }
                Text("Operation: ${operation.action}", fontSize = 12.sp)
                if (operation.action == "Coupon Used") {
                    Text("Amount Used: ₪${operation.changeSummary}", fontSize = 12.sp)
                } else {
                    Text(operation.changeSummary, fontSize = 12.sp)
                }
                Text(
                    text = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()).format(operation.timestamp),
                    fontSize = 12.sp
                )
            }
            if (!operation.action.equals("Coupon Created", ignoreCase = true)) {
                IconButton(onClick = { onUndo(operation) }) {
                    Icon(Icons.Filled.Undo, contentDescription = "Undo")
                }
            }
        }
    }
}
