package com.nimroddayan.couponmanager

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.ui.screen.AddCouponDialog
import com.nimroddayan.couponmanager.ui.screen.ArchivedCouponsScreen
import com.nimroddayan.couponmanager.ui.screen.CategoryManagementScreen
import com.nimroddayan.couponmanager.ui.screen.DashboardScreen
import com.nimroddayan.couponmanager.ui.screen.HomeScreen
import com.nimroddayan.couponmanager.ui.screen.SettingsScreen
import com.nimroddayan.couponmanager.ui.theme.CouponManagerTheme
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModelFactory
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModelFactory
import com.nimroddayan.couponmanager.ui.viewmodel.ThemeViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.ThemeViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Home : Screen("home", Icons.Filled.Home, "Home")
    object Dashboard : Screen("dashboard", Icons.Filled.Dashboard, "Dashboard")
    object Settings : Screen("settings", Icons.Filled.Settings, "Settings")
    object CategoryManagement : Screen("category_management", Icons.Filled.Home, "Manage Categories") // Icon isn't used here
    object ArchivedCoupons : Screen("archived_coupons", Icons.Filled.Archive, "Archived Coupons")
}

@Composable
fun App(app: CouponApplication) {
    val context = LocalContext.current
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModelFactory(context))
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()

    CouponManagerTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "main") {
            composable(
                "main",
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) }
            ) {
                val couponViewModel: CouponViewModel = viewModel(factory = CouponViewModelFactory(app.database.couponDao()))
                val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(app.database.categoryDao()))

                MainScaffold(
                    couponViewModel = couponViewModel,
                    categoryViewModel = categoryViewModel,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { themeViewModel.setTheme(it) },
                    onManageCategories = { navController.navigate(Screen.CategoryManagement.route) },
                    onResetDatabase = {
                        couponViewModel.clearAll()
                        categoryViewModel.clearAll()
                    },
                    onNavigateToArchive = { navController.navigate(Screen.ArchivedCoupons.route) }
                )
            }
            composable(
                Screen.CategoryManagement.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(app.database.categoryDao()))
                CategoryManagementScreen(
                    viewModel = categoryViewModel,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
            composable(
                Screen.ArchivedCoupons.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                ArchivedCouponsScreen(
                    app = app,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScaffold(
    couponViewModel: CouponViewModel,
    categoryViewModel: CategoryViewModel,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onManageCategories: () -> Unit,
    onResetDatabase: () -> Unit,
    onNavigateToArchive: () -> Unit
) {
    val coupons by couponViewModel.allCoupons.collectAsState(initial = emptyList())
    var showAddCouponDialog by remember { mutableStateOf(false) }

    val screens = listOf(
        Screen.Home,
        Screen.Dashboard,
        Screen.Settings,
    )
    val pagerState = rememberPagerState { screens.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    screens.forEachIndexed { index, screen ->
                        val selected = pagerState.currentPage == index
                        val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

                        Column(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(screen.icon, contentDescription = screen.title, tint = contentColor)
                            Text(text = screen.title, color = contentColor, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            HorizontalPager(state = pagerState) {
                page ->
                when (page) {
                    0 -> HomeScreen(
                        coupons = coupons,
                        categoriesViewModel = categoryViewModel,
                        couponViewModel = couponViewModel,
                        onAddCoupon = { showAddCouponDialog = true }
                    )
                    1 -> DashboardScreen()
                    2 -> SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = onThemeChange,
                        onManageCategories = onManageCategories,
                        onResetDatabase = onResetDatabase,
                        onNavigateToArchive = onNavigateToArchive
                    )
                }
            }

            if (showAddCouponDialog) {
                AddCouponDialog(
                    categoryViewModel = categoryViewModel,
                    onAddCoupon = { name, value, expiration, categoryId ->
                        val newCoupon = Coupon(
                            name = name,
                            currentValue = value,
                            initialValue = value,
                            expirationDate = expiration,
                            categoryId = categoryId
                        )
                        couponViewModel.insert(newCoupon)
                    },
                    onDismiss = { showAddCouponDialog = false },
                    onAddCategory = {
                        showAddCouponDialog = false
                        onManageCategories()
                    }
                )
            }
        }
    }
}
