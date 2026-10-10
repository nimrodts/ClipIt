package com.nimroddayan.clipit.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.model.Category
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.data.model.SortOption
import com.nimroddayan.clipit.ui.components.CategoryDot
import com.nimroddayan.clipit.ui.components.CouponCardShape
import com.nimroddayan.clipit.ui.components.CouponSheetBody
import com.nimroddayan.clipit.ui.components.CouponSheetHeader
import com.nimroddayan.clipit.ui.components.PendingCouponCard
import com.nimroddayan.clipit.ui.components.RedeemPass
import com.nimroddayan.clipit.ui.components.SectionCaption
import com.nimroddayan.clipit.ui.components.SmallButton
import com.nimroddayan.clipit.ui.components.SwipeableCouponCard
import com.nimroddayan.clipit.ui.components.categoryColor
import com.nimroddayan.clipit.ui.components.pressScale
import com.nimroddayan.clipit.ui.components.redeemValue
import com.nimroddayan.clipit.ui.components.rememberHaptics
import com.nimroddayan.clipit.ui.motion.Motion
import com.nimroddayan.clipit.ui.motion.projectedDistance
import com.nimroddayan.clipit.ui.motion.rubberband
import com.nimroddayan.clipit.ui.motion.springStiffness
import com.nimroddayan.clipit.ui.theme.extendedColors
import com.nimroddayan.clipit.ui.viewmodel.CategoryViewModel
import com.nimroddayan.clipit.ui.viewmodel.CouponViewModel
import com.nimroddayan.clipit.util.formatMoney
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@OptIn(ExperimentalSharedTransitionApi::class)
private val SheetBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 1f, stiffness = springStiffness(0.38f), visibilityThreshold = Rect.VisibilityThreshold)
}

private fun SortOption.caption(): String =
        when (this) {
            SortOption.DateAddedDesc -> "Newest first"
            SortOption.DateAddedAsc -> "Oldest first"
            SortOption.DateAsc -> "Expiring first"
            SortOption.DateDesc -> "Expiring last"
            SortOption.NameAsc -> "A to Z"
            SortOption.NameDesc -> "Z to A"
        }

private fun List<Coupon>.sortedBy(option: SortOption): List<Coupon> =
        sortedWith(
                when (option) {
                    SortOption.NameAsc -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                    SortOption.NameDesc -> compareByDescending(String.CASE_INSENSITIVE_ORDER) { it.name }
                    SortOption.DateAsc -> compareBy { it.expirationDate }
                    SortOption.DateDesc -> compareByDescending { it.expirationDate }
                    SortOption.DateAddedAsc -> compareBy { it.id }
                    SortOption.DateAddedDesc -> compareByDescending { it.id }
                }
        )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
        coupons: List<Coupon>,
        categoriesViewModel: CategoryViewModel,
        couponViewModel: CouponViewModel,
        onAddCoupon: () -> Unit,
        onNavigateToHistory: (Long) -> Unit,
        bottomPadding: Dp = 0.dp,
        onSheetOpenChange: (Boolean) -> Unit = {},
) {
    val categories by categoriesViewModel.allCategories.collectAsState(initial = emptyList())
    val sortOption by couponViewModel.sortOption.collectAsState()
    val currencySymbol by couponViewModel.currencySymbol.collectAsState()
    val pendingCoupons by couponViewModel.pendingCoupons.collectAsState(initial = emptyList())
    val aiRetryingIds by couponViewModel.aiRetryingIds.collectAsState()

    val ext = MaterialTheme.extendedColors
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryIds by rememberSaveable { mutableStateOf(emptySet<Long>()) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    var focusUse by remember { mutableStateOf(false) }
    var pass by remember { mutableStateOf<Pair<Coupon, Offset?>?>(null) }
    var editCoupon by remember { mutableStateOf<Coupon?>(null) }
    var hiddenPendingIds by remember { mutableStateOf(emptySet<Long>()) }
    val cardCenters = remember { mutableMapOf<Long, Offset>() }

    fun categoryOf(c: Coupon): Category? = categories.find { it.id == c.categoryId }

    fun showMessage(message: String, onUndo: (() -> Unit)? = null, onExpire: (() -> Unit)? = null) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            var undone = false
            try {
                val result =
                        snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = if (onUndo != null) "Undo" else null,
                                duration = SnackbarDuration.Short
                        )
                if (result == SnackbarResult.ActionPerformed && onUndo != null) {
                    undone = true
                    onUndo()
                }
            } finally {
                // Also runs if the screen goes away mid-snackbar, so a dismissal is never lost.
                if (!undone) onExpire?.invoke()
            }
        }
    }

    LaunchedEffect(couponViewModel) {
        couponViewModel.undoableChanges.collect { change ->
            showMessage(change.message, onUndo = {
                haptics.tick()
                couponViewModel.undo(change)
            })
        }
    }

    val filteredCoupons =
            coupons.filter { c ->
                c.name.contains(searchQuery, ignoreCase = true) &&
                        (selectedCategoryIds.isEmpty() || c.categoryId in selectedCategoryIds)
            }
                    .sortedBy(sortOption)
    val visiblePending = pendingCoupons.filter { it.id !in hiddenPendingIds }

    // The sheet keeps showing the last coupon while it animates away.
    val selectedCoupon = coupons.find { it.id == selectedId }
    var lastSheetCoupon by remember { mutableStateOf<Coupon?>(null) }
    if (selectedCoupon != null) lastSheetCoupon = selectedCoupon
    LaunchedEffect(selectedId, selectedCoupon) {
        // Fully used or removed elsewhere: let the sheet go.
        if (selectedId != null && selectedCoupon == null) selectedId = null
    }
    val sheetOpen = selectedId != null && selectedCoupon != null
    LaunchedEffect(sheetOpen) { onSheetOpenChange(sheetOpen) }

    val dragY = remember { Animatable(0f) }
    var sheetHeightPx by remember { mutableFloatStateOf(1f) }
    val openProgress by animateFloatAsState(if (sheetOpen) 1f else 0f, Motion.sheet(), label = "sheetDepth")
    val depth = openProgress * (1f - (dragY.value / sheetHeightPx).coerceIn(0f, 1f))

    fun openSheet(c: Coupon, use: Boolean = false) {
        focusManager.clearFocus()
        focusUse = use
        scope.launch { dragY.snapTo(0f) }
        selectedId = c.id
    }
    fun closeSheet() {
        focusManager.clearFocus()
        selectedId = null
    }
    BackHandler(enabled = sheetOpen) { closeSheet() }

    val listState = rememberLazyListState()
    LaunchedEffect(listState.isScrollInProgress) { if (listState.isScrollInProgress) focusManager.clearFocus() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    SharedTransitionLayout(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                    modifier =
                            Modifier.graphicsLayer {
                                        // Dim to focus: push the list back while the sheet is up.
                                        val s = 1f - 0.055f * depth
                                        scaleX = s
                                        scaleY = s
                                        if (depth > 0.001f) {
                                            clip = true
                                            shape = RoundedCornerShape((24 * depth).dp)
                                        }
                                    }
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        LargeTopAppBar(
                                title = { Text("Coupons") },
                                actions = {
                                    Box {
                                        IconButton(onClick = { showSortMenu = true }) {
                                            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                                        }
                                        SortMenu(
                                                expanded = showSortMenu,
                                                selected = sortOption,
                                                onSelect = {
                                                    couponViewModel.saveSortOption(it)
                                                    showSortMenu = false
                                                },
                                                onDismiss = { showSortMenu = false }
                                        )
                                    }
                                    IconButton(onClick = onAddCoupon) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add coupon", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                colors =
                                        TopAppBarDefaults.largeTopAppBarColors(
                                                containerColor = MaterialTheme.colorScheme.background,
                                                scrolledContainerColor = ext.bar,
                                        ),
                                scrollBehavior = scrollBehavior
                        )
                    }
            ) { innerPadding ->
                LazyColumn(
                        state = listState,
                        contentPadding =
                                PaddingValues(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = innerPadding.calculateTopPadding() + 4.dp,
                                        bottom = bottomPadding + 24.dp
                                ),
                        modifier = Modifier.fillMaxSize()
                ) {
                    item(key = "search") {
                        SearchField(searchQuery, onQueryChange = { searchQuery = it }, onDone = { focusManager.clearFocus() })
                    }
                    if (categories.isNotEmpty()) {
                        item(key = "chips") {
                            CategoryChips(
                                    categories = categories,
                                    selectedIds = selectedCategoryIds,
                                    onToggle = { id ->
                                        selectedCategoryIds =
                                                if (id in selectedCategoryIds) selectedCategoryIds - id else selectedCategoryIds + id
                                    },
                                    onClear = { selectedCategoryIds = emptySet() }
                            )
                        }
                    }
                    if (visiblePending.isNotEmpty()) {
                        item(key = "pending-caption") {
                            SectionCaption(
                                    "Found in your SMS",
                                    trailing = visiblePending.size.toString(),
                                    modifier = Modifier.animateItem().padding(top = 22.dp, bottom = 10.dp)
                            )
                        }
                        items(visiblePending, key = { "pending-${it.id}" }) { p ->
                            PendingCouponCard(
                                    coupon = p,
                                    category = categoryOf(p),
                                    currencySymbol = currencySymbol,
                                    isRetryingAi = p.id in aiRetryingIds,
                                    onAdd = {
                                        haptics.confirm()
                                        couponViewModel.approve(p)
                                        showMessage("Added ${p.name}")
                                    },
                                    onDismiss = {
                                        // Hidden now, deleted once the Undo window passes.
                                        hiddenPendingIds = hiddenPendingIds + p.id
                                        showMessage(
                                                "Dismissed ${p.name}",
                                                onUndo = { hiddenPendingIds = hiddenPendingIds - p.id },
                                                onExpire = {
                                                    couponViewModel.reject(p)
                                                    hiddenPendingIds = hiddenPendingIds - p.id
                                                }
                                        )
                                    },
                                    onEdit = { editCoupon = p },
                                    onRetryAi = { couponViewModel.retryAi(p) },
                                    modifier = Modifier.animateItem().padding(bottom = 10.dp)
                            )
                        }
                    }
                    item(key = "list-caption") {
                        SectionCaption(
                                sortOption.caption(),
                                trailing = if (coupons.isEmpty()) null else "${filteredCoupons.size} of ${coupons.size}",
                                modifier = Modifier.animateItem().padding(top = 22.dp, bottom = 10.dp)
                        )
                    }
                    if (filteredCoupons.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                    hasCoupons = coupons.isNotEmpty(),
                                    query = searchQuery,
                                    onClear = {
                                        searchQuery = ""
                                        selectedCategoryIds = emptySet()
                                    },
                                    onAdd = onAddCoupon
                            )
                        }
                    }
                    items(filteredCoupons, key = { it.id }) { coupon ->
                        var heightPx by remember { mutableIntStateOf(0) }
                        Box(
                                Modifier.animateItem()
                                        .padding(bottom = 10.dp)
                                        // Hold the row's space while its card is up in the sheet.
                                        .heightIn(min = with(density) { heightPx.toDp() })
                        ) {
                            AnimatedVisibility(
                                    visible = selectedId != coupon.id,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                            ) {
                                SwipeableCouponCard(
                                        coupon = coupon,
                                        category = categoryOf(coupon),
                                        currencySymbol = currencySymbol,
                                        onClick = { openSheet(coupon) },
                                        onLongPress = {
                                            if (coupon.redeemValue() != null) pass = coupon to cardCenters[coupon.id]
                                            else showMessage("No redeem code saved for ${coupon.name}")
                                        },
                                        onSwipeUse = { openSheet(coupon, use = true) },
                                        onSwipeRedeem = { couponViewModel.redeemOneTimeUndoable(coupon) },
                                        onSwipeArchive = { couponViewModel.archiveUndoable(coupon) },
                                        modifier =
                                                Modifier.onSizeChanged { heightPx = it.height }
                                                        .onGloballyPositioned { cardCenters[coupon.id] = it.boundsInWindow().center },
                                        cardModifier =
                                                Modifier.sharedBounds(
                                                        rememberSharedContentState(key = "coupon-${coupon.id}"),
                                                        animatedVisibilityScope = this@AnimatedVisibility,
                                                        boundsTransform = SheetBoundsTransform,
                                                        clipInOverlayDuringTransition = OverlayClip(CouponCardShape)
                                                )
                                )
                            }
                        }
                    }
                }
            }

            // Scrim
            if (depth > 0.001f) {
                Box(
                        Modifier.fillMaxSize()
                                .graphicsLayer { alpha = depth }
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                                .pointerInput(Unit) { detectTapGestures { closeSheet() } }
                )
            }

            // Detail sheet: morphs out of the card it was opened from.
            AnimatedVisibility(
                    visible = sheetOpen,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.fillMaxSize()
            ) {
                val c = lastSheetCoupon
                if (c != null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        Column(
                                Modifier.fillMaxWidth()
                                        .fillMaxHeight(0.92f)
                                        .onSizeChanged { sheetHeightPx = it.height.toFloat().coerceAtLeast(1f) }
                                        .offset { IntOffset(0, dragY.value.roundToInt()) }
                                        .sharedBounds(
                                                rememberSharedContentState(key = "coupon-${c.id}"),
                                                animatedVisibilityScope = this@AnimatedVisibility,
                                                boundsTransform = SheetBoundsTransform,
                                                clipInOverlayDuringTransition = OverlayClip(SheetShape)
                                        )
                                        .clip(SheetShape)
                                        .background(ext.sheet)
                        ) {
                            CouponSheetHeader(
                                    coupon = c,
                                    category = categoryOf(c),
                                    currencySymbol = currencySymbol,
                                    onDone = ::closeSheet,
                                    modifier =
                                            Modifier.pointerInput(c.id) {
                                                val tracker = VelocityTracker()
                                                var raw = 0f
                                                detectVerticalDragGestures(
                                                        onDragStart = {
                                                            tracker.resetTracking()
                                                            raw = dragY.value
                                                            scope.launch { dragY.stop() }
                                                        },
                                                        onVerticalDrag = { change, dy ->
                                                            change.consume()
                                                            raw += dy
                                                            // The header moves with the finger, so track the
                                                            // accumulated drag rather than local positions.
                                                            tracker.addPosition(change.uptimeMillis, Offset(0f, raw))
                                                            val next = if (raw < 0f) -rubberband(-raw, sheetHeightPx) else raw
                                                            scope.launch { dragY.snapTo(next) }
                                                        },
                                                        onDragEnd = {
                                                            val v = tracker.calculateVelocity().y
                                                            val projected = dragY.value + projectedDistance(v)
                                                            if (projected > sheetHeightPx * 0.28f && v > -100f) {
                                                                closeSheet()
                                                            } else {
                                                                scope.launch { dragY.animateTo(0f, Motion.momentum(0.3f), v) }
                                                            }
                                                        },
                                                        onDragCancel = { scope.launch { dragY.animateTo(0f, Motion.standard(0.3f)) } }
                                                )
                                            }
                            )
                            CouponSheetBody(
                                    coupon = c,
                                    currencySymbol = currencySymbol,
                                    focusUse = focusUse,
                                    onUse = { amount ->
                                        couponViewModel.useUndoable(
                                                c,
                                                amount,
                                                "Used ${formatMoney(amount, currencySymbol)} from ${c.name}"
                                        )
                                    },
                                    onRedeem = {
                                        closeSheet()
                                        couponViewModel.redeemOneTimeUndoable(c)
                                    },
                                    onShowCode = { origin -> pass = c to origin },
                                    onHistory = {
                                        closeSheet()
                                        onNavigateToHistory(c.id)
                                    },
                                    onEdit = { editCoupon = c },
                                    onArchive = {
                                        closeSheet()
                                        couponViewModel.archiveUndoable(c)
                                    },
                                    onDelete = {
                                        closeSheet()
                                        couponViewModel.delete(c)
                                        showMessage("Deleted ${c.name}")
                                    },
                                    modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            SnackbarHost(
                    snackbarHostState,
                    modifier =
                            Modifier.align(Alignment.BottomCenter)
                                    .then(if (sheetOpen) Modifier.navigationBarsPadding() else Modifier.padding(bottom = bottomPadding))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) { data ->
                Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(14.dp),
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        actionColor = MaterialTheme.colorScheme.inversePrimary,
                )
            }
        }
    }

    pass?.let { (coupon, origin) ->
        RedeemPass(coupon = coupon, category = categoryOf(coupon), originInWindow = origin, onDismiss = { pass = null })
    }

    editCoupon?.let { coupon ->
        EditCouponDialog(
                coupon = coupon,
                categoryViewModel = categoriesViewModel,
                couponViewModel = couponViewModel,
                onDismiss = { editCoupon = null }
        )
    }
}

@Composable
private fun SortMenu(expanded: Boolean, selected: SortOption, onSelect: (SortOption) -> Unit, onDismiss: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        listOf(
                SortOption.DateAddedDesc to "Date added (newest)",
                SortOption.DateAddedAsc to "Date added (oldest)",
                SortOption.DateAsc to "Expiration (soonest)",
                SortOption.DateDesc to "Expiration (latest)",
                SortOption.NameAsc to "Name (A–Z)",
                SortOption.NameDesc to "Name (Z–A)",
        ).forEach { (option, label) ->
            DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { onSelect(option) },
                    trailingIcon = { if (option == selected) Icon(Icons.Filled.Check, contentDescription = "Selected") }
            )
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onDone: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    Row(
            Modifier.fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ext.fillStrong)
                    .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text("Search coupons", style = MaterialTheme.typography.bodyLarge, color = ext.inkTertiary)
            }
            BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onDone() }),
                    modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Outlined.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun CategoryChips(
        categories: List<Category>,
        selectedIds: Set<Long>,
        onToggle: (Long) -> Unit,
        onClear: () -> Unit,
) {
    LazyRow(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "all") { Chip("All", selected = selectedIds.isEmpty(), dot = null, onClick = onClear) }
        items(categories, key = { it.id }) { category ->
            Chip(category.name, selected = category.id in selectedIds, dot = categoryColor(category.colorHex)) { onToggle(category.id) }
        }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, dot: Color?, onClick: () -> Unit) {
    val ext = MaterialTheme.extendedColors
    val interaction = remember { MutableInteractionSource() }
    Row(
            Modifier.pressScale(interaction, 0.95f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selected) MaterialTheme.colorScheme.onSurface else ext.card)
                    .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                    .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot != null) {
            CategoryDot(dot, size = 7.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
                label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyState(hasCoupons: Boolean, query: String, onClear: () -> Unit, onAdd: () -> Unit) {
    Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
                if (hasCoupons) "No coupons match" else "No coupons yet",
                style = MaterialTheme.typography.titleLarge,
        )
        Text(
                when {
                    !hasCoupons -> "Add one with +, or ClipIt will find them in your SMS."
                    query.isNotBlank() -> "Nothing matches “$query” with these filters."
                    else -> "No coupons in the selected categories."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        if (hasCoupons) SmallButton("Clear filters", filled = false, onClick = onClear)
        else SmallButton("Add coupon", filled = true, onClick = onAdd)
    }
}
