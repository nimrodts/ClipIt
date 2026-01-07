package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nimroddayan.couponmanager.data.model.MonthlySpending
import com.nimroddayan.couponmanager.ui.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val spendingByCategory by viewModel.spendingByCategory.collectAsState()
    val spendingByMonth by viewModel.spendingByMonth.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column {
            Text("Overview", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
            Text("Financial Dashboard", style = MaterialTheme.typography.headlineMedium)
        }

        // Metrics Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Balance",
                value = totalBalance,
                icon = Icons.Default.AccountBalanceWallet,
                color = MaterialTheme.colorScheme.primary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Total Spent",
                value = totalSpent,
                icon = Icons.Default.ShoppingCart,
                color = MaterialTheme.colorScheme.error // Using error color for spending usually looks good, or Secondary
            )
        }

        // Custom Segmented Control for Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf("By Category", "By Month")
            tabs.forEachIndexed { index, title ->
                val selected = selectedTab == index
                val containerColor = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
                val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(containerColor)
                        .clickable { selectedTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = title, style = MaterialTheme.typography.labelLarge, color = contentColor)
                }
            }
        }

        when (selectedTab) {
            0 -> {
                if (spendingByCategory.isNotEmpty()) {
                    ElevatedCard(
                        elevation = CardDefaults.elevatedCardElevation(2.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Spending Distribution", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(24.dp))
                            DonutChart(data = spendingByCategory)
                        }
                    }
                }
            }

            1 -> {
                if (spendingByMonth.isNotEmpty()) {
                    SpendingByMonth(spendingByMonth)
                }
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier = Modifier, title: String, value: Double, icon: ImageVector, color: Color) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape)
                    .padding(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.fillMaxSize())
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "₪%.2f".format(value), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun DonutChart(data: List<com.nimroddayan.couponmanager.data.model.CategorySpending>) {
    val total = data.sumOf { it.totalSpent }
    val proportions = data.map { it.totalSpent / total }
    val colors = listOf(Color(0xFF5E81AC), Color(0xFF81A1C1), Color(0xFF88C0D0), Color(0xFFA3BE8C), Color(0xFFEBCB8B), Color(0xFFD08770), Color(0xFFBF616A))
    
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onSurface)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f // Start from top
                
                proportions.forEachIndexed { index, proportion ->
                    val sweep = (proportion * 360).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweep
                }
            }
            // Center Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total", style = MaterialTheme.typography.labelMedium)
                Text("₪%.0f".format(total), style = MaterialTheme.typography.titleLarge)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Legend Grid
        Column(modifier = Modifier.fillMaxWidth()) {
            data.forEachIndexed { index, categorySpending ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = categorySpending.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Text(text = "₪%.2f".format(categorySpending.totalSpent), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (index < data.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(start = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun SpendingByMonth(spendingByMonth: List<MonthlySpending>) {
    var expanded by remember { mutableStateOf(false) }
    val chartData = if (expanded) spendingByMonth else spendingByMonth.takeLast(6)

    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Trends", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (expanded) "Show Less" else "View All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SpendingByMonthChart(data = chartData)
            
            if (expanded) {
                Spacer(modifier = Modifier.height(24.dp))
                MonthlySpendingTable(data = spendingByMonth.reversed())
            }
        }
    }
}

@Composable
fun SpendingByMonthChart(data: List<MonthlySpending>) {
    val maxValue = data.maxOfOrNull { it.totalSpent } ?: 0.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val highestSpendingValue = data.maxOfOrNull { it.totalSpent }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall.copy(color = onSurface)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center // Center the graph
    ) {
        val yAxisLabels = (0..4).map { (it * maxValue / 4).toFloat() }

        // Bars and X-Axis Labels
        Canvas(modifier = Modifier.fillMaxSize()) { 
            // New calculation: 40% width bar, distributed evenly
            val slotWidth = size.width / data.size
            val barWidth = slotWidth * 0.2f 

            val monthNameFormatter = SimpleDateFormat("MMM", Locale.getDefault())
            
            // Dashed grid lines
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            yAxisLabels.forEach { label ->
                val y = size.height - (label / maxValue.toFloat() * size.height)
                drawLine(
                    color = onSurface.copy(alpha = 0.1f), // Lighter grid
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = pathEffect
                )
            }

            data.forEachIndexed { index, monthlySpending ->
                val barHeight = (monthlySpending.totalSpent / maxValue * size.height).toFloat()
                
                // Centered within its slot
                val startX = (index * slotWidth) + (slotWidth - barWidth) / 2

                // Determine bar color - Highlight max spending
                val color = if (monthlySpending.totalSpent == highestSpendingValue) secondaryColor else primaryColor

                // Draw rounded bar
                drawRoundRect(
                    color = color,
                    topLeft = Offset(startX, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Draw month label
                val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthlySpending.month)
                val monthName = date?.let { monthNameFormatter.format(it) } ?: ""
                val monthLayoutResult = textMeasurer.measure(AnnotatedString(monthName), style = labelStyle)
                drawText(
                    textLayoutResult = monthLayoutResult,
                    topLeft = Offset(
                        x = startX + barWidth / 2 - monthLayoutResult.size.width / 2,
                        y = size.height + 6.dp.toPx()
                    )
                )

                // Draw value label above the bar
                val valueText = "₪${monthlySpending.totalSpent.toInt()}"
                val valueLayoutResult = textMeasurer.measure(AnnotatedString(valueText), style = labelStyle)
                drawText(
                    textLayoutResult = valueLayoutResult,
                    topLeft = Offset(
                        x = startX + barWidth / 2 - valueLayoutResult.size.width / 2,
                        y = size.height - barHeight - valueLayoutResult.size.height - 4.dp.toPx()
                    )
                )
            }
        }
    }
}


@Composable
fun MonthlySpendingTable(data: List<MonthlySpending>) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Month", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text("Total Spent", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
            data.forEach {
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it.month, style = MaterialTheme.typography.bodyLarge)
                    Text("₪%.2f".format(it.totalSpent), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                if (data.last() != it) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                }
            }
        }
    }
}
