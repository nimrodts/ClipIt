package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.data.model.SortOption
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
    var showOneTimeRedeemDialog by remember { mutableStateOf<Coupon?>(null) }
    var showRedeemCodeDialog by remember { mutableStateOf<String?>(null) }
    var showOriginalMessageDialog by remember { mutableStateOf<String?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(emptySet<Category>()) }
    val sortOption by couponViewModel.sortOption.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredCoupons =
            coupons
                    .filter { coupon ->
                        val searchMatch = coupon.name.contains(searchQuery, ignoreCase = true)
                        val categoryMatch =
                                selectedCategories.isEmpty() ||
                                        selectedCategories.any { it.id == coupon.categoryId }
                        searchMatch && categoryMatch
                    }
                    .sortedWith(
                            when (sortOption) {
                                SortOption.NameAsc ->
                                        compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                                SortOption.NameDesc ->
                                        compareByDescending(String.CASE_INSENSITIVE_ORDER) {
                                            it.name
                                        }
                                SortOption.DateAsc -> compareBy { it.expirationDate }
                                SortOption.DateDesc -> compareByDescending { it.expirationDate }
                                SortOption.DateAddedAsc -> compareBy { it.id }
                                SortOption.DateAddedDesc -> compareByDescending { it.id }
                            }
                    )

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    Scaffold(
            modifier =
                    Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = onAddCoupon,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                ) { Icon(Icons.Filled.Add, contentDescription = "Add Coupon") }
            }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Coupons") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                        colors =
                                androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                        focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                        modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sort")
                    }
                    DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                                text = { Text("Date Added (Newest)") },
                                onClick = {
                                    couponViewModel.saveSortOption(SortOption.DateAddedDesc)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOption == SortOption.DateAddedDesc)
                                            Icon(Icons.Default.Check, null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Date Added (Oldest)") },
                                onClick = {
                                    couponViewModel.saveSortOption(SortOption.DateAddedAsc)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOption == SortOption.DateAddedAsc)
                                            Icon(Icons.Default.Check, null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Expiration (Soonest)") },
                                onClick = {
                                    couponViewModel.saveSortOption(SortOption.DateAsc)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOption == SortOption.DateAsc)
                                            Icon(Icons.Default.Check, null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Expiration (Latest)") },
                                onClick = {
                                    couponViewModel.saveSortOption(SortOption.DateDesc)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOption == SortOption.DateDesc)
                                            Icon(Icons.Default.Check, null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Name (A-Z)") },
                                onClick = {
                                    couponViewModel.saveSortOption(SortOption.NameAsc)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOption == SortOption.NameAsc)
                                            Icon(Icons.Default.Check, null)
                                }
                        )
                        DropdownMenuItem(
                                text = { Text("Name (Z-A)") },
                                onClick = {
                                    couponViewModel.saveSortOption(SortOption.NameDesc)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (sortOption == SortOption.NameDesc)
                                            Icon(Icons.Default.Check, null)
                                }
                        )
                    }
                }
            }

            LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                            selected = selectedCategories.contains(category),
                            onClick = {
                                selectedCategories =
                                        if (selectedCategories.contains(category)) {
                                            selectedCategories - category
                                        } else {
                                            selectedCategories + category
                                        }
                            },
                            label = { Text(category.name) }
                    )
                }
            }

            if (filteredCoupons.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No coupons found. Try a different filter!")
                }
            } else {
                LazyColumn(
                        state = listState,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCoupons) { coupon ->
                        val category = categories.find { it.id == coupon.categoryId }
                        CouponItem(
                                coupon = coupon,
                                category = category,
                                onUseClick = {
                                    if (coupon.isOneTime) {
                                        showOneTimeRedeemDialog = coupon
                                    } else {
                                        showUseCouponDialog = coupon
                                    }
                                },
                                onEditClick = { showEditCouponDialog = coupon },
                                onArchiveClick = { showArchiveConfirmationDialog = coupon },
                                onDeleteClick = { showDeleteConfirmationDialog = coupon },
                                onLongPress = { showRedeemCodeDialog = coupon.redeemCode },
                                onHistoryClick = { onNavigateToHistory(coupon.id) },
                                onViewMessageClick = {
                                    showOriginalMessageDialog = coupon.creationMessage
                                }
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
                message =
                        "Are you sure you want to delete this coupon? This action cannot be undone."
        )
    }

    showOneTimeRedeemDialog?.let { coupon ->
        ConfirmationDialog(
                onConfirm = {
                    couponViewModel.redeemOneTime(coupon)
                    showOneTimeRedeemDialog = null
                },
                onDismiss = { showOneTimeRedeemDialog = null },
                title = "Redeem Coupon",
                message =
                        "Are you sure you want to redeem this coupon? It will be marked as used and archived."
        )
    }

    showRedeemCodeDialog?.let { redeemCode ->
        RedeemCodeDialog(redeemCode = redeemCode, onDismiss = { showRedeemCodeDialog = null })
    }

    showOriginalMessageDialog?.let { message ->
        MessageDialog(message = message, onDismiss = { showOriginalMessageDialog = null })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CouponItem(
        coupon: Coupon,
        category: Category?,
        onUseClick: () -> Unit,
        onEditClick: () -> Unit,
        onArchiveClick: () -> Unit,
        onDeleteClick: () -> Unit,
        onLongPress: () -> Unit,
        onHistoryClick: () -> Unit,
        onViewMessageClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    val categoryName = category?.name ?: "Uncategorized"
    val categoryColor = category?.colorHex ?: "#808080"
    val categoryIcon = category?.iconName ?: "help"

    ElevatedCard(
            modifier =
                    Modifier.fillMaxWidth()
                            .combinedClickable(
                                    onClick = { /* No action on single click */},
                                    onLongClick = onLongPress
                            ),
            elevation = CardDefaults.elevatedCardElevation(2.dp),
            colors =
                    CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                    )
    ) {
        Column {
            Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                        modifier =
                                Modifier.size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                                Color(
                                                        android.graphics.Color.parseColor(
                                                                categoryColor
                                                        )
                                                )
                                        )
                ) {
                    Icon(
                            imageVector = getIconByName(categoryIcon),
                            contentDescription = categoryName,
                            modifier = Modifier.align(Alignment.Center),
                            tint = getContrastColor(categoryColor)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = coupon.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = categoryName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                    )

                    if (coupon.isOneTime) {
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.Surface(
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                        ) {
                            Text(
                                    text = "1x Single Use",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val expirationText =
                            "Expires: ${SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(Date(coupon.expirationDate))}"
                    val expirationColor =
                            if (coupon.expirationDate < System.currentTimeMillis())
                                    MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(text = expirationText, color = expirationColor, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (!coupon.isOneTime) {
                        Text(
                                text = "₪${String.format("%.2f", coupon.currentValue)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                                text = "/ ₪${String.format("%.2f", coupon.initialValue)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        IconButton(onClick = onUseClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                    if (coupon.isOneTime) Icons.Default.Check
                                    else Icons.Default.ShoppingCart,
                                    contentDescription = "Use Coupon",
                                    modifier = Modifier.size(20.dp)
                            )
                        }
                        Box {
                            IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                        modifier = Modifier.size(20.dp)
                                )
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
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                )
                                DropdownMenuItem(
                                        text = { Text("Archive") },
                                        onClick = {
                                            onArchiveClick()
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                    Icons.Default.Archive,
                                                    contentDescription = "Archive"
                                            )
                                        }
                                )
                                DropdownMenuItem(
                                        text = { Text("History") },
                                        onClick = {
                                            onHistoryClick()
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                    Icons.Default.History,
                                                    contentDescription = "History"
                                            )
                                        }
                                )
                                coupon.creationMessage?.let {
                                    DropdownMenuItem(
                                            text = { Text("View Original Message") },
                                            onClick = {
                                                onViewMessageClick()
                                                showMenu = false
                                            },
                                            leadingIcon = {
                                                Icon(
                                                        Icons.Default.Message,
                                                        contentDescription = "View Original Message"
                                                )
                                            }
                                    )
                                }
                                DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            onDeleteClick()
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete"
                                            )
                                        }
                                )
                            }
                        }
                    }
                }
            }
            if (!coupon.isOneTime) {
                LinearProgressIndicator(
                        progress = {
                            (coupon.currentValue / coupon.initialValue).toFloat().coerceIn(0f, 1f)
                        },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
