package com.nimroddayan.clipit.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.data.model.MonthlySpending
import com.nimroddayan.clipit.ui.components.CategoryDot
import com.nimroddayan.clipit.ui.components.GroupDivider
import com.nimroddayan.clipit.ui.components.GroupRow
import com.nimroddayan.clipit.ui.components.GroupedList
import com.nimroddayan.clipit.ui.components.SectionCaption
import com.nimroddayan.clipit.ui.components.categoryColor
import com.nimroddayan.clipit.ui.components.expiryColor
import com.nimroddayan.clipit.ui.theme.extendedColors
import com.nimroddayan.clipit.ui.theme.tabular
import com.nimroddayan.clipit.ui.viewmodel.DashboardViewModel
import com.nimroddayan.clipit.util.expiryLabel
import com.nimroddayan.clipit.util.formatMoney
import com.nimroddayan.clipit.util.formatMoneyShort
import java.text.SimpleDateFormat
import java.util.Locale

/** Insights: what's available to spend, what's about to expire, and where money went. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
        viewModel: DashboardViewModel,
        coupons: List<Coupon> = emptyList(),
        bottomPadding: Dp = 0.dp,
) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val spendingByCategory by viewModel.spendingByCategory.collectAsState()
    val spendingByMonth by viewModel.spendingByMonth.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val ext = MaterialTheme.extendedColors

    val now = System.currentTimeMillis()
    val active = coupons.filter { expiryLabel(it.expirationDate, now).daysLeft >= 0 }
    val giftCards = active.count { !it.isOneTime }
    val singles = active.count { it.isOneTime }
    val expiringSoon =
            active.filter { expiryLabel(it.expirationDate, now).daysLeft <= 30 }.sortedBy { it.expirationDate }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                LargeTopAppBar(
                        title = { Text("Insights") },
                        colors =
                                TopAppBarDefaults.largeTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        scrolledContainerColor = ext.bar
                                ),
                        scrollBehavior = scrollBehavior
                )
            }
    ) { padding ->
        LazyColumn(
                contentPadding =
                        PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = padding.calculateTopPadding() + 4.dp,
                                bottom = bottomPadding + 24.dp
                        ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
        ) {
            item(key = "hero") {
                GroupedList {
                    Column(Modifier.padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("AVAILABLE TO SPEND", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatMoney(totalBalance, currencySymbol), style = MaterialTheme.typography.displayLarge)
                        Text(
                                "$giftCards gift card${if (giftCards == 1) "" else "s"} and $singles single-use voucher${if (singles == 1) "" else "s"} · " +
                                        "${formatMoney(totalSpent, currencySymbol)} spent so far",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item(key = "soon-caption") { SectionCaption("Expiring in the next 30 days", Modifier.padding(top = 14.dp)) }
            item(key = "soon") {
                GroupedList {
                    if (expiringSoon.isEmpty()) {
                        GroupRow {
                            Text("Nothing expires in the next 30 days.", style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    expiringSoon.forEachIndexed { i, c ->
                        if (i > 0) GroupDivider()
                        val e = expiryLabel(c.expirationDate, now)
                        GroupRow {
                            Text(c.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(e.text, style = MaterialTheme.typography.bodySmall, color = expiryColor(e), fontWeight = FontWeight.Medium)
                                Text(
                                        if (c.isOneTime) "Single use" else formatMoney(c.currentValue, currencySymbol),
                                        style = MaterialTheme.typography.bodyMedium.tabular,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (spendingByCategory.isNotEmpty()) {
                item(key = "cat-caption") { SectionCaption("Spent by category", Modifier.padding(top = 14.dp)) }
                item(key = "cats") {
                    val max = spendingByCategory.maxOf { it.totalSpent }.takeIf { it > 0 } ?: 1.0
                    GroupedList {
                        spendingByCategory.sortedByDescending { it.totalSpent }.forEachIndexed { i, s ->
                            if (i > 0) GroupDivider()
                            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CategoryDot(categoryColor(s.colorHex))
                                    Spacer(Modifier.width(8.dp))
                                    Text(s.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                    Text(formatMoney(s.totalSpent, currencySymbol), style = MaterialTheme.typography.bodyMedium.tabular,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                CategoryBar((s.totalSpent / max).toFloat(), categoryColor(s.colorHex))
                            }
                        }
                    }
                }
            }

            if (spendingByMonth.isNotEmpty()) {
                item(key = "month-caption") { SectionCaption("Spent per month", Modifier.padding(top = 14.dp)) }
                item(key = "months") { MonthlySpendingCard(spendingByMonth, currencySymbol) }
            }
        }
    }
}

@Composable
private fun CategoryBar(fraction: Float, color: androidx.compose.ui.graphics.Color) {
    Canvas(Modifier.fillMaxWidth().height(6.dp).clip(MaterialTheme.shapes.extraSmall)) {
        drawRoundRect(
                color = color,
                size = Size(size.width * fraction.coerceIn(0.02f, 1f), size.height),
                cornerRadius = CornerRadius(size.height / 2)
        )
    }
}

private fun monthLabel(month: String, pattern: String): String =
        try {
            SimpleDateFormat("yyyy-MM", Locale.US).parse(month)?.let { SimpleDateFormat(pattern, Locale.getDefault()).format(it) } ?: month
        } catch (e: Exception) {
            month
        }

/**
 * Single-series monthly bars: thin accent bars anchored to a baseline, recessive grid, labels in
 * text colors. The latest month is labeled; tapping a bar shows its amount. A table view expands below.
 */
@Composable
private fun MonthlySpendingCard(data: List<MonthlySpending>, currencySymbol: String) {
    val ext = MaterialTheme.extendedColors
    var selected by rememberSaveable { mutableIntStateOf(data.lastIndex) }
    var showTable by rememberSaveable { mutableStateOf(false) }
    val measurer = rememberTextMeasurer()
    val accent = MaterialTheme.colorScheme.primary
    val inkSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ink = MaterialTheme.colorScheme.onSurface
    val grid = ext.hairline
    val labelStyle = MaterialTheme.typography.bodySmall
    val valueStyle = MaterialTheme.typography.labelMedium.tabular.copy(fontWeight = FontWeight.SemiBold)
    val max = data.maxOf { it.totalSpent }
    val summary =
            data.joinToString { "${monthLabel(it.month, "MMMM")}: ${formatMoneyShort(it.totalSpent, currencySymbol)}" }

    GroupedList {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            val sel = data.getOrNull(selected)
            if (sel != null) {
                Text(monthLabel(sel.month, "MMMM yyyy"), style = MaterialTheme.typography.bodySmall, color = inkSecondary)
                Text(formatMoney(sel.totalSpent, currencySymbol), style = MaterialTheme.typography.titleLarge.tabular)
            }
            Spacer(Modifier.height(12.dp))
            Canvas(
                    Modifier.fillMaxWidth()
                            .height(150.dp)
                            .semantics { contentDescription = "Monthly spending. $summary" }
                            .pointerInput(data) {
                                detectTapGestures { pos ->
                                    val slot = size.width / data.size.toFloat()
                                    selected = (pos.x / slot).toInt().coerceIn(0, data.lastIndex)
                                }
                            }
            ) {
                val labelHeight = 20.dp.toPx()
                val top = 18.dp.toPx()
                val chartHeight = size.height - labelHeight - top
                val baseline = top + chartHeight
                // Recessive grid: baseline plus a midline.
                drawLine(grid, Offset(0f, baseline), Offset(size.width, baseline), 1.dp.toPx())
                drawLine(grid, Offset(0f, top + chartHeight / 2), Offset(size.width, top + chartHeight / 2), 1.dp.toPx())
                val slot = size.width / data.size
                val barWidth = (slot * 0.36f).coerceAtMost(28.dp.toPx())
                data.forEachIndexed { i, m ->
                    val h = if (max > 0) (m.totalSpent / max).toFloat() * chartHeight else 0f
                    val x = i * slot + (slot - barWidth) / 2
                    val isSelected = i == selected
                    if (h > 0f) {
                        drawTopRoundedBar(
                                x, baseline - h, barWidth, h, 4.dp.toPx(),
                                if (isSelected) accent else accent.copy(alpha = 0.45f)
                        )
                    }
                    val label = measurer.measure(monthLabel(m.month, "MMM"), labelStyle.copy(color = if (isSelected) ink else inkSecondary))
                    drawText(label, topLeft = Offset(x + barWidth / 2 - label.size.width / 2, baseline + 4.dp.toPx()))
                    if (isSelected && m.totalSpent > 0) {
                        val value = measurer.measure(formatMoneyShort(m.totalSpent, currencySymbol), valueStyle.copy(color = ink))
                        val vx = (x + barWidth / 2 - value.size.width / 2).coerceIn(0f, size.width - value.size.width)
                        drawText(value, topLeft = Offset(vx, baseline - h - value.size.height - 2.dp.toPx()))
                    }
                }
            }
        }
        GroupDivider()
        GroupRow(onClick = { showTable = !showTable }) {
            Text(if (showTable) "Hide table" else "Show as table", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
        }
        AnimatedVisibility(showTable, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column {
                data.reversed().forEach { m ->
                    GroupDivider()
                    GroupRow {
                        Text(monthLabel(m.month, "MMMM yyyy"), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(formatMoney(m.totalSpent, currencySymbol), style = MaterialTheme.typography.bodyLarge.tabular,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

/** A bar with rounded data-end and a square base on the baseline. */
private fun DrawScope.drawTopRoundedBar(
        x: Float, y: Float, w: Float, h: Float, radius: Float, color: androidx.compose.ui.graphics.Color
) {
    val r = radius.coerceAtMost(minOf(w / 2, h))
    val path =
            Path().apply {
                moveTo(x, y + h)
                lineTo(x, y + r)
                quadraticBezierTo(x, y, x + r, y)
                lineTo(x + w - r, y)
                quadraticBezierTo(x + w, y, x + w, y + r)
                lineTo(x + w, y + h)
                close()
            }
    drawPath(path, color)
}
