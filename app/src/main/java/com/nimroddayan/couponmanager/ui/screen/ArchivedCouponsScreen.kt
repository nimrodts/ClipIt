package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nimroddayan.couponmanager.CouponApplication
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedCouponsScreen(app: CouponApplication, onNavigateUp: () -> Unit) {
    val couponViewModel: CouponViewModel = viewModel(factory = CouponViewModelFactory(app.database.couponDao()))
    val archivedCoupons by couponViewModel.archivedCoupons.collectAsState()

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
                // You can reuse the CouponItem composable here, 
                // but you might want to create a simpler version for the archive.
                Text(text = coupon.name)
            }
        }
    }
}
