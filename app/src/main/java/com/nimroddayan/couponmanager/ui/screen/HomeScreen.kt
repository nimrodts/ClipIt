package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import com.nimroddayan.couponmanager.util.getContrastColor
import com.nimroddayan.couponmanager.util.getIconByName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    coupons: List<Coupon>,
    categoriesViewModel: CategoryViewModel,
    couponViewModel: CouponViewModel,
    onAddCoupon: () -> Unit,
    onNavigateToHistory: (Long) -> Unit,
) {
    val categories by categoriesViewModel.allCategories.collectAsState(initial = emptyList())
    var showUseCouponDialog by remember { mutableStateOf<Coupon?>(null) }
    var showEditCouponDialog by remember { mutableStateOf<Coupon?>(null) }
    var showArchiveConfirmationDialog by remember { mutableStateOf<Coupon?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf<Coupon?>(null) }
    var showRedeemCodeDialog by remember { mutableStateOf<String?>(null) }
    var showOriginalMessageDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCoupon) {
                Icon(Icons.Filled.Add, contentDescription = "Add Coupon")
            }
        }
    ) { paddingValues ->
        if (coupons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No coupons yet. Tap the + button to add one!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues)) {
                items(coupons) { coupon ->
                    val category = categories.find { it.id == coupon.categoryId }
                    if (category != null) {
                        CouponItem(
                            coupon = coupon,
                            category = category,
                            onUseClick = { showUseCouponDialog = coupon },
                            onEditClick = { showEditCouponDialog = coupon },
                            onArchiveClick = { showArchiveConfirmationDialog = coupon },
                            onDeleteClick = { showDeleteConfirmationDialog = coupon },
                            onLongPress = { showRedeemCodeDialog = coupon.redeemCode },
                            onHistoryClick = { onNavigateToHistory(coupon.id) },
                            onViewMessageClick = { showOriginalMessageDialog = coupon.creationMessage }
                        )
                    }
                }
            }
        }
    }

    showUseCouponDialog?.let { coupon ->
        UseCouponDialog(
            coupon = coupon,
            onConfirm = { amount ->
                couponViewModel.use(coupon, amount)
                showUseCouponDialog = null
            },
            onDismiss = { showUseCouponDialog = null }
        )
    }

    showEditCouponDialog?.let { coupon ->
        EditCouponDialog(
            coupon = coupon,
            categoryViewModel = categoriesViewModel,
            couponViewModel = couponViewModel,
            onDismiss = { showEditCouponDialog = null }
        )
    }

    showArchiveConfirmationDialog?.let { coupon ->
        ConfirmationDialog(
            onConfirm = {
                couponViewModel.archive(coupon)
                showArchiveConfirmationDialog = null
            },
            onDismiss = { showArchiveConfirmationDialog = null },
            title = "Archive Coupon",
            message = "Are you sure you want to archive this coupon?"
        )
    }

    showDeleteConfirmationDialog?.let { coupon ->
        ConfirmationDialog(
            onConfirm = {
                couponViewModel.delete(coupon)
                showDeleteConfirmationDialog = null
            },
            onDismiss = { showDeleteConfirmationDialog = null },
            title = "Delete Coupon",
            message = "Are you sure you want to delete this coupon? This action cannot be undone."
        )
    }

    showRedeemCodeDialog?.let { redeemCode ->
        RedeemCodeDialog(
            redeemCode = redeemCode,
            onDismiss = { showRedeemCodeDialog = null }
        )
    }

    showOriginalMessageDialog?.let { message ->
        MessageDialog(
            message = message,
            onDismiss = { showOriginalMessageDialog = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CouponItem(
    coupon: Coupon,
    category: com.nimroddayan.couponmanager.data.model.Category,
    onUseClick: () -> Unit,
    onEditClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLongPress: () -> Unit,
    onHistoryClick: () -> Unit,
    onViewMessageClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.combinedClickable(
            onClick = { /* No action on single click */ },
            onLongClick = onLongPress
        )
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(category.colorHex)))
                ) {
                    Icon(
                        imageVector = getIconByName(category.iconName),
                        contentDescription = category.name,
                        modifier = Modifier.align(Alignment.Center),
                        tint = getContrastColor(category.colorHex)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = coupon.name, fontWeight = FontWeight.Bold)
                    Text(text = category.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
                    val expirationText = "Expires: ${SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(coupon.expirationDate))}"
                    val expirationColor = if (coupon.expirationDate < System.currentTimeMillis()) MaterialTheme.colorScheme.error else Color.Unspecified
                    Text(text = expirationText, color = expirationColor, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₪${String.format("%.2f", coupon.currentValue)} / ₪${String.format("%.2f", coupon.initialValue)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        IconButton(onClick = onUseClick) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Use Coupon")
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        onEditClick()
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Archive") },
                                    onClick = {
                                        onArchiveClick()
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Archive, contentDescription = "Archive") }
                                )
                                DropdownMenuItem(
                                    text = { Text("History") },
                                    onClick = {
                                        onHistoryClick()
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.History, contentDescription = "History") }
                                )
                                coupon.creationMessage?.let {
                                    DropdownMenuItem(
                                        text = { Text("View Original Message") },
                                        onClick = {
                                            onViewMessageClick()
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Message, contentDescription = "View Original Message") }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        onDeleteClick()
                                        showMenu = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                                )
                            }
                        }
                    }
                }
            }
            LinearProgressIndicator(
                progress = { (coupon.currentValue / coupon.initialValue).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        HorizontalDivider()
    }
}
