package com.nimroddayan.couponmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nimroddayan.couponmanager.data.DatabaseManager
import com.nimroddayan.couponmanager.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.ui.screen.AddCouponDialog
import com.nimroddayan.couponmanager.ui.screen.AiSettingsScreen
import com.nimroddayan.couponmanager.ui.screen.ArchivedCouponsScreen
import com.nimroddayan.couponmanager.ui.screen.CategoryManagementScreen
import com.nimroddayan.couponmanager.ui.screen.CouponHistoryScreen
import com.nimroddayan.couponmanager.ui.screen.DashboardScreen
import com.nimroddayan.couponmanager.ui.screen.DatabaseSettingsScreen
import com.nimroddayan.couponmanager.ui.screen.HomeScreen
import com.nimroddayan.couponmanager.ui.screen.SettingsScreen
import com.nimroddayan.couponmanager.ui.theme.CouponManagerTheme
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModelFactory
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.DashboardViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.DashboardViewModelFactory
import com.nimroddayan.couponmanager.ui.viewmodel.HistoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.HistoryViewModelFactory
import com.nimroddayan.couponmanager.ui.viewmodel.SettingsViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
    object Home : Screen("home", Icons.Filled.Home, "Home")
    object Dashboard : Screen("dashboard", Icons.Filled.Dashboard, "Dashboard")
    object Settings : Screen("settings", Icons.Filled.Settings, "Settings")
    object CategoryManagement : Screen("category_management", Icons.Filled.Home, "Manage Categories")
    object ArchivedCoupons : Screen("archived_coupons", Icons.Filled.Archive, "Archived Coupons")
    object AiSettings : Screen("ai_settings", Icons.Filled.SmartToy, "AI Settings")
    object CouponHistory : Screen("coupon_history/{couponId}", Icons.Filled.Home, "History")
    object DatabaseSettings : Screen("database_settings", Icons.Filled.Dns, "Database")
}

@Composable
fun App(app: CouponApplication) {
    val context = LocalContext.current
    val themeViewModel: com.nimroddayan.couponmanager.ui.viewmodel.ThemeViewModel = viewModel(factory = com.nimroddayan.couponmanager.ui.viewmodel.ThemeViewModelFactory(context))
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val databaseManager = remember { DatabaseManager(context) }

    val viewModelFactory = ViewModelFactory(
        context,
        app.couponRepository,
        GeminiApiKeyRepository(context)
    )

    CouponManagerTheme(darkTheme = isDarkTheme) {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "main") {
            composable(
                "main",
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) }
            ) {
                MainScaffold(
                    app = app,
                    viewModelFactory = viewModelFactory,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { themeViewModel.setTheme(it) },
                    onManageCategories = { navController.navigate(Screen.CategoryManagement.route) },
                    onNavigateToArchive = { navController.navigate(Screen.ArchivedCoupons.route) },
                    onNavigateToAiSettings = { navController.navigate(Screen.AiSettings.route) },
                    onNavigateToHistory = { couponId -> navController.navigate("coupon_history/$couponId") },
                    onNavigateToDatabaseSettings = { navController.navigate(Screen.DatabaseSettings.route) }
                )
            }
            composable(
                Screen.CategoryManagement.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(app.database))
                CategoryManagementScreen(
                    viewModel = categoryViewModel,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
            composable(
                Screen.ArchivedCoupons.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                ArchivedCouponsScreen(
                    app = app,
                    onNavigateUp = { navController.popBackStack() },
                    onNavigateToHistory = { couponId -> navController.navigate("coupon_history/$couponId") }
                )
            }
            composable(
                Screen.AiSettings.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                AiSettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateUp = { navController.popBackStack() }
                )
            }
            composable(
                Screen.CouponHistory.route,
                arguments = listOf(navArgument("couponId") { type = NavType.LongType }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                val couponId = it.arguments?.getLong("couponId") ?: -1
                val historyViewModelFactory = HistoryViewModelFactory(app.database.couponHistoryDao(), app.couponRepository)
                CouponHistoryScreen(
                    couponId = couponId,
                    viewModelFactory = historyViewModelFactory
                )
            }
            composable(
                Screen.DatabaseSettings.route,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
            ) {
                DatabaseSettingsScreen(
                    onNavigateUp = { navController.popBackStack() },
                    onResetDatabase = {
                        databaseManager.resetDatabase()
                        restartApp(context)
                    },
                )
            }
        }
    }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    (context as? Activity)?.finish()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScaffold(
    app: CouponApplication,
    viewModelFactory: ViewModelFactory,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onManageCategories: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToAiSettings: () -> Unit,
    onNavigateToHistory: (Long) -> Unit,
    onNavigateToDatabaseSettings: () -> Unit,
) {
    val couponViewModel: CouponViewModel = viewModel(factory = viewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModelFactory(app.database))
    val dashboardViewModel: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(app.database))

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
            HorizontalPager(state = pagerState) { page ->
                when (screens[page]) {
                    Screen.Home -> HomeScreen(
                        coupons = coupons,
                        categoriesViewModel = categoryViewModel,
                        couponViewModel = couponViewModel,
                        onAddCoupon = { showAddCouponDialog = true },
                        onNavigateToHistory = onNavigateToHistory
                    )

                    Screen.Dashboard -> DashboardScreen(viewModel = dashboardViewModel)
                    Screen.Settings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeChange = onThemeChange,
                        onManageCategories = onManageCategories,
                        onNavigateToArchive = onNavigateToArchive,
                        onNavigateToAiSettings = onNavigateToAiSettings,
                        onNavigateToDatabaseSettings = onNavigateToDatabaseSettings
                    )

                    else -> {}
                }
            }

            if (showAddCouponDialog) {
                AddCouponDialog(
                    categoryViewModel = categoryViewModel,
                    couponViewModel = couponViewModel,
                    onAddCoupon = { name, value, expiration, categoryId, redeemCode, creationMessage, onSuccess ->
                        val newCoupon = Coupon(
                            name = name,
                            currentValue = value,
                            initialValue = value,
                            expirationDate = expiration,
                            categoryId = categoryId,
                            redeemCode = redeemCode,
                            creationMessage = creationMessage,
                        )
                        couponViewModel.insert(newCoupon, onSuccess)
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
