package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
    onAddCoupon: () -> Unit
) {
    val categories by categoriesViewModel.allCategories.collectAsState()
    var showUseCouponDialog by remember { mutableStateOf<Coupon?>(null) }

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
                            onUseClick = { showUseCouponDialog = coupon }
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
                val newBalance = coupon.currentValue - amount
                val updatedCoupon = if (newBalance <= 0) {
                    coupon.copy(currentValue = 0.0, isArchived = true)
                } else {
                    coupon.copy(currentValue = newBalance)
                }
                couponViewModel.update(updatedCoupon)
            },
            onDismiss = { showUseCouponDialog = null }
        )
    }
}

@Composable
fun CouponItem(
    coupon: Coupon, 
    category: com.nimroddayan.couponmanager.data.model.Category, 
    onUseClick: () -> Unit
) {
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
                Column {
                    Text(
                        text = "₪${String.format("%.2f", coupon.currentValue)} / ₪${String.format("%.2f", coupon.initialValue)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onUseClick) {
                        Text("Use")
                    }
                }
            }
            LinearProgressIndicator(
                progress = { (coupon.currentValue / coupon.initialValue).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Divider()
    }
}
