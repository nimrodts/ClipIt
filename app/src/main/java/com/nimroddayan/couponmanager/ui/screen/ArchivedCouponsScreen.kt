package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nimroddayan.couponmanager.CouponApplication
import com.nimroddayan.couponmanager.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModelFactory
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.ViewModelFactory
import com.nimroddayan.couponmanager.util.getIconByName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedCouponsScreen(
    app: CouponApplication,
    onNavigateUp: () -> Unit,
    onNavigateToHistory: (Long) -> Unit,
) {
    val context = LocalContext.current
    val viewModelFactory = ViewModelFactory(
        context,
        app.couponRepository,
        GeminiApiKeyRepository(context)
    )
    val couponViewModel: CouponViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(app.database.categoryDao()))
    val archivedCoupons by couponViewModel.archivedCoupons.collectAsState(initial = emptyList())
    val categories by categoryViewModel.allCategories.collectAsState(initial = emptyList())

    var showUnarchiveDialog by remember { mutableStateOf<Coupon?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Coupon?>(null) }

    showUnarchiveDialog?.let { coupon ->
        ConfirmationDialog(
            onConfirm = {
                couponViewModel.unarchive(coupon)
                showUnarchiveDialog = null
            },
            onDismiss = { showUnarchiveDialog = null },
            title = "Unarchive Coupon",
            message = "Are you sure you want to unarchive this coupon?"
        )
    }

    showDeleteDialog?.let { coupon ->
        ConfirmationDialog(
            onConfirm = {
                couponViewModel.delete(coupon)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null },
            title = "Delete Coupon",
            message = "Are you sure you want to delete this coupon? This action cannot be undone."
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archived Coupons") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(archivedCoupons) { coupon ->
                val category = categories.find { it.id == coupon.categoryId }
                if (category != null) {
                    ArchivedCouponItem(
                        coupon = coupon,
                        category = category,
                        onUnarchive = { showUnarchiveDialog = coupon },
                        onDelete = { showDeleteDialog = coupon },
                        onHistoryClick = { onNavigateToHistory(coupon.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ArchivedCouponItem(
    coupon: Coupon,
    category: com.nimroddayan.couponmanager.data.model.Category,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    onHistoryClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface {
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
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = coupon.name, fontWeight = FontWeight.Bold)
                    Text(text = category.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(top = 4.dp))
                    val expirationText = "Used on: ${SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(coupon.expirationDate))}"
                    Text(text = expirationText, fontSize = 12.sp)
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
                            text = { Text("Unarchive") },
                            onClick = {
                                onUnarchive()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Unarchive, contentDescription = "Unarchive") }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = {
                                onHistoryClick()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = "History") }
                        )
                    }
                }
            }
        }
    }
}
