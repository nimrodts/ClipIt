package com.nimroddayan.clipit

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nimroddayan.clipit.data.DatabaseManager
import com.nimroddayan.clipit.data.gemini.GeminiApiKeyRepository
import com.nimroddayan.clipit.data.model.Coupon
import com.nimroddayan.clipit.ui.components.pressScale
import com.nimroddayan.clipit.ui.motion.Motion
import com.nimroddayan.clipit.ui.screen.AddCouponDialog
import com.nimroddayan.clipit.ui.screen.AiSettingsScreen
import com.nimroddayan.clipit.ui.screen.ArchivedCouponsScreen
import com.nimroddayan.clipit.ui.screen.CategoryManagementScreen
import com.nimroddayan.clipit.ui.screen.CouponHistoryScreen
import com.nimroddayan.clipit.ui.screen.DashboardScreen
import com.nimroddayan.clipit.ui.screen.DatabaseSettingsScreen
import com.nimroddayan.clipit.ui.screen.HomeScreen
import com.nimroddayan.clipit.ui.screen.SettingsScreen
import com.nimroddayan.clipit.ui.theme.ClipItTheme
import com.nimroddayan.clipit.ui.theme.extendedColors
import com.nimroddayan.clipit.ui.viewmodel.CategoryViewModel
import com.nimroddayan.clipit.ui.viewmodel.CategoryViewModelFactory
import com.nimroddayan.clipit.ui.viewmodel.CouponViewModel
import com.nimroddayan.clipit.ui.viewmodel.DashboardViewModel
import com.nimroddayan.clipit.ui.viewmodel.DashboardViewModelFactory
import com.nimroddayan.clipit.ui.viewmodel.HistoryViewModelFactory
import com.nimroddayan.clipit.ui.viewmodel.SettingsViewModel
import com.nimroddayan.clipit.ui.viewmodel.ViewModelFactory

sealed class Screen(val route: String, val icon: ImageVector, val title: String) {
        object Home : Screen("home", Icons.Outlined.ConfirmationNumber, "Coupons")
        object Dashboard : Screen("dashboard", Icons.Outlined.Insights, "Insights")
        object Settings : Screen("settings", Icons.Outlined.Settings, "Settings")
        object CategoryManagement :
                Screen("category_management", Icons.Filled.Home, "Manage Categories")
        object ArchivedCoupons :
                Screen("archived_coupons", Icons.Filled.Archive, "Archived Coupons")
        object AiSettings : Screen("ai_settings", Icons.Filled.SmartToy, "AI Settings")
        object CouponHistory : Screen("coupon_history/{couponId}", Icons.Filled.Home, "History")
        object DatabaseSettings : Screen("database_settings", Icons.Filled.Dns, "Database")
        object WhitelistManagement :
                Screen("whitelist_management", Icons.Filled.SmartToy, "Whitelist")
}

@Composable
fun App(app: CouponApplication, startDestination: String? = null) {
        val context = LocalContext.current
        val themeViewModel: com.nimroddayan.clipit.ui.viewmodel.ThemeViewModel =
                viewModel(
                        factory = com.nimroddayan.clipit.ui.viewmodel.ThemeViewModelFactory(context)
                )
        val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
        val databaseManager = remember { DatabaseManager(context) }

        val userPreferencesRepository = remember {
                com.nimroddayan.clipit.data.UserPreferencesRepository(context)
        }
        val viewModelFactory =
                ViewModelFactory(
                        context,
                        app.couponRepository,
                        GeminiApiKeyRepository(context),
                        userPreferencesRepository
                )

        ClipItTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()

                val permissions =
                        mutableListOf(
                                android.Manifest.permission.RECEIVE_SMS,
                        )

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                }

                val launcher =
                        androidx.activity.compose.rememberLauncherForActivityResult(
                                androidx.activity.result.contract.ActivityResultContracts
                                        .RequestMultiplePermissions()
                        ) { _ -> }

                LaunchedEffect(Unit) { launcher.launch(permissions.toTypedArray()) }

                LaunchedEffect(startDestination) {
                        if (startDestination == "database_settings") {
                                navController.navigate(Screen.DatabaseSettings.route)
                        }
                }

                NavHost(navController = navController, startDestination = "main") {
                        composable(
                                "main",
                                exitTransition = { PushExit },
                                popEnterTransition = { PopEnter }
                        ) {
                                MainScaffold(
                                        app = app,
                                        viewModelFactory = viewModelFactory,
                                        userPreferencesRepository = userPreferencesRepository,
                                        isDarkTheme = isDarkTheme,
                                        onThemeChange = { themeViewModel.setTheme(it) },
                                        onManageCategories = {
                                                navController.navigate(
                                                        Screen.CategoryManagement.route
                                                )
                                        },
                                        onNavigateToArchive = {
                                                navController.navigate(Screen.ArchivedCoupons.route)
                                        },
                                        onNavigateToAiSettings = {
                                                navController.navigate(Screen.AiSettings.route)
                                        },
                                        onNavigateToHistory = { couponId ->
                                                navController.navigate("coupon_history/$couponId")
                                        },
                                        onNavigateToDatabaseSettings = {
                                                navController.navigate(
                                                        Screen.DatabaseSettings.route
                                                )
                                        },
                                        onManageWhitelist = {
                                                navController.navigate(
                                                        Screen.WhitelistManagement.route
                                                )
                                        }
                                )
                        }
                        composable(
                                Screen.CategoryManagement.route,
                                enterTransition = { PushEnter },
                                exitTransition = { PushExit },
                                popEnterTransition = { PopEnter },
                                popExitTransition = { PopExit }
                        ) {
                                val categoryViewModel: CategoryViewModel =
                                        viewModel(factory = CategoryViewModelFactory(app.database))
                                CategoryManagementScreen(
                                        viewModel = categoryViewModel,
                                        onNavigateUp = { navController.popBackStack() }
                                )
                        }
                        composable(
                                Screen.ArchivedCoupons.route,
                                enterTransition = { PushEnter },
                                exitTransition = { PushExit },
                                popEnterTransition = { PopEnter },
                                popExitTransition = { PopExit }
                        ) {
                                ArchivedCouponsScreen(
                                        app = app,
                                        onNavigateUp = { navController.popBackStack() },
                                        onNavigateToHistory = { couponId ->
                                                navController.navigate("coupon_history/$couponId")
                                        }
                                )
                        }
                        composable(
                                Screen.AiSettings.route,
                                enterTransition = { PushEnter },
                                exitTransition = { PushExit },
                                popEnterTransition = { PopEnter },
                                popExitTransition = { PopExit }
                        ) {
                                val settingsViewModel: SettingsViewModel =
                                        viewModel(factory = viewModelFactory)
                                AiSettingsScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateUp = { navController.popBackStack() }
                                )
                        }
                        composable(
                                Screen.WhitelistManagement.route,
                                enterTransition = { PushEnter },
                                exitTransition = { PushExit },
                                popEnterTransition = { PopEnter },
                                popExitTransition = { PopExit }
                        ) {
                                val settingsViewModel: SettingsViewModel =
                                        viewModel(factory = viewModelFactory)
                                com.nimroddayan.clipit.ui.screen.WhitelistManagementScreen(
                                        viewModel = settingsViewModel,
                                        onNavigateUp = { navController.popBackStack() }
                                )
                        }
                        composable(
                                Screen.CouponHistory.route,
                                arguments =
                                        listOf(navArgument("couponId") { type = NavType.LongType }),
                                enterTransition = { PushEnter },
                                exitTransition = { PushExit },
                                popEnterTransition = { PopEnter },
                                popExitTransition = { PopExit }
                        ) {
                                val couponId = it.arguments?.getLong("couponId") ?: -1
                                val historyViewModelFactory =
                                        HistoryViewModelFactory(
                                                app.database.couponHistoryDao(),
                                                app.couponRepository,
                                                userPreferencesRepository
                                        )
                                CouponHistoryScreen(
                                        couponId = couponId,
                                        viewModelFactory = historyViewModelFactory
                                )
                        }
                        composable(
                                Screen.DatabaseSettings.route,
                                enterTransition = { PushEnter },
                                exitTransition = { PushExit },
                                popEnterTransition = { PopEnter },
                                popExitTransition = { PopExit }
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


@Composable
fun MainScaffold(
        app: CouponApplication,
        viewModelFactory: ViewModelFactory,
        userPreferencesRepository: com.nimroddayan.clipit.data.UserPreferencesRepository,
        isDarkTheme: Boolean,
        onThemeChange: (Boolean) -> Unit,
        onManageCategories: () -> Unit,
        onNavigateToArchive: () -> Unit,
        onNavigateToAiSettings: () -> Unit,
        onNavigateToHistory: (Long) -> Unit,
        onNavigateToDatabaseSettings: () -> Unit,
        onManageWhitelist: () -> Unit,
) {
        val couponViewModel: CouponViewModel = viewModel(factory = viewModelFactory)
        val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
        val categoryViewModel: CategoryViewModel =
                viewModel(factory = CategoryViewModelFactory(app.database))
        val dashboardViewModel: DashboardViewModel =
                viewModel(
                        factory = DashboardViewModelFactory(app.database, userPreferencesRepository)
                )

        val coupons by couponViewModel.allCoupons.collectAsState(initial = emptyList())
        var showAddCouponDialog by remember { mutableStateOf(false) }
        var sheetOpen by remember { mutableStateOf(false) }

        val tabs = listOf(Screen.Home, Screen.Dashboard, Screen.Settings)
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        val stateHolder = rememberSaveableStateHolder()
        val tabBarHeight =
                TabBarHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                // No paging between tabs: horizontal swipes belong to the coupon cards.
                Crossfade(targetState = selectedTab, animationSpec = Motion.quick(), label = "tabs") { tab ->
                        stateHolder.SaveableStateProvider(tab) {
                                when (tabs[tab]) {
                                        Screen.Home ->
                                                HomeScreen(
                                                        coupons = coupons,
                                                        categoriesViewModel = categoryViewModel,
                                                        couponViewModel = couponViewModel,
                                                        onAddCoupon = { showAddCouponDialog = true },
                                                        onNavigateToHistory = onNavigateToHistory,
                                                        bottomPadding = tabBarHeight,
                                                        onSheetOpenChange = { sheetOpen = it }
                                                )
                                        Screen.Dashboard ->
                                                DashboardScreen(
                                                        viewModel = dashboardViewModel,
                                                        coupons = coupons,
                                                        bottomPadding = tabBarHeight
                                                )
                                        Screen.Settings ->
                                                SettingsScreen(
                                                        viewModel = settingsViewModel,
                                                        isDarkTheme = isDarkTheme,
                                                        onThemeChange = onThemeChange,
                                                        onManageCategories = onManageCategories,
                                                        onNavigateToArchive = onNavigateToArchive,
                                                        onNavigateToAiSettings = onNavigateToAiSettings,
                                                        onNavigateToDatabaseSettings =
                                                                onNavigateToDatabaseSettings,
                                                        onManageWhitelist = onManageWhitelist,
                                                        bottomPadding = tabBarHeight
                                                )
                                        else -> {}
                                }
                        }
                }

                // The sheet covers the tab bar, so the bar steps aside while it is up.
                val hidden by
                        animateFloatAsState(
                                if (sheetOpen) 1f else 0f,
                                Motion.standard(),
                                label = "tabBarHidden"
                        )
                TabBar(
                        tabs = tabs,
                        selected = selectedTab,
                        onSelect = { selectedTab = it },
                        modifier =
                                Modifier.align(Alignment.BottomCenter).graphicsLayer {
                                        translationY = size.height * hidden
                                        alpha = 1f - hidden
                                }
                )

                if (showAddCouponDialog) {
                        AddCouponDialog(
                                categoryViewModel = categoryViewModel,
                                couponViewModel = couponViewModel,
                                onAddCoupon = {
                                        name,
                                        value,
                                        expiration,
                                        categoryId,
                                        redeemCode,
                                        redemptionUrl,
                                        creationMessage,
                                        isOneTime,
                                        onSuccess ->
                                        val newCoupon =
                                                Coupon(
                                                        name = name,
                                                        currentValue = value,
                                                        initialValue = value,
                                                        expirationDate = expiration,
                                                        categoryId = categoryId,
                                                        redeemCode = redeemCode,
                                                        redemptionUrl = redemptionUrl,
                                                        creationMessage = creationMessage,
                                                        isOneTime = isOneTime,
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

private val TabBarHeight = 58.dp

// Push/pop: the new screen slides over while the old one drifts back a quarter, on one spring.
private val PushEnter = slideInHorizontally(Motion.standard(0.4f)) { it }
private val PushExit =
        slideOutHorizontally(Motion.standard(0.4f)) { -it / 4 } + fadeOut(Motion.standard(0.4f), 0.6f)
private val PopEnter =
        slideInHorizontally(Motion.standard(0.4f)) { -it / 4 } + fadeIn(Motion.standard(0.4f), 0.6f)
private val PopExit = slideOutHorizontally(Motion.standard(0.4f)) { it }

/** Translucent tab bar; content scrolls under it. Tabs give feedback on touch-down. */
@Composable
private fun TabBar(
        tabs: List<Screen>,
        selected: Int,
        onSelect: (Int) -> Unit,
        modifier: Modifier = Modifier
) {
        val ext = MaterialTheme.extendedColors
        Column(modifier.fillMaxWidth().background(ext.bar)) {
                HorizontalDivider(thickness = 0.5.dp, color = ext.hairline)
                Row(
                        Modifier.fillMaxWidth().navigationBarsPadding().height(TabBarHeight),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        tabs.forEachIndexed { index, screen ->
                                val isSelected = index == selected
                                val interaction = remember { MutableInteractionSource() }
                                val color =
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else ext.inkTertiary
                                Column(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .fillMaxHeight()
                                                        .pressScale(interaction, 0.9f)
                                                        .selectable(
                                                                selected = isSelected,
                                                                interactionSource = interaction,
                                                                indication = null,
                                                                role = Role.Tab,
                                                                onClick = { onSelect(index) }
                                                        ),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                ) {
                                        Icon(screen.icon, contentDescription = null, tint = color)
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                                screen.title,
                                                color = color,
                                                style =
                                                        MaterialTheme.typography.labelSmall.copy(
                                                                letterSpacing = 0.01.em
                                                        )
                                        )
                                }
                        }
                }
        }
}
