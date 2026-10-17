package com.nimroddayan.clipit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nimroddayan.clipit.data.model.Currency
import com.nimroddayan.clipit.ui.components.GroupDivider
import com.nimroddayan.clipit.ui.components.GroupRow
import com.nimroddayan.clipit.ui.components.GroupedList
import com.nimroddayan.clipit.ui.components.SectionCaption
import com.nimroddayan.clipit.ui.theme.extendedColors
import com.nimroddayan.clipit.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        viewModel: SettingsViewModel,
        isDarkTheme: Boolean,
        onThemeChange: (Boolean) -> Unit,
        onManageCategories: () -> Unit,
        onNavigateToArchive: () -> Unit,
        onNavigateToAiSettings: () -> Unit,
        onNavigateToDatabaseSettings: () -> Unit,
        onManageWhitelist: () -> Unit,
        bottomPadding: Dp = 0.dp,
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val currentCurrency by viewModel.selectedCurrency.collectAsState()
    val ext = MaterialTheme.extendedColors
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                LargeTopAppBar(
                        title = { Text("Settings") },
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
            item {
                SectionCaption("Appearance")
            }
            item {
                GroupedList {
                    GroupRow(onClick = { onThemeChange(!isDarkTheme) }) {
                        RowIcon(Icons.Outlined.DarkMode)
                        Text("Dark mode", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onThemeChange,
                                colors =
                                        SwitchDefaults.colors(
                                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                                checkedThumbColor = Color.White,
                                                uncheckedTrackColor = ext.fillStrong,
                                                uncheckedThumbColor = Color.White,
                                                uncheckedBorderColor = Color.Transparent,
                                        )
                        )
                    }
                    GroupDivider()
                    NavRow(Icons.Outlined.Payments, "Currency", Currency.fromCode(currentCurrency).let { "${it.symbol} ${it.code}" }) {
                        showCurrencyDialog = true
                    }
                }
            }
            item { SectionCaption("Coupons", Modifier.padding(top = 14.dp)) }
            item {
                GroupedList {
                    NavRow(Icons.Outlined.Category, "Categories", onClick = onManageCategories)
                    GroupDivider()
                    NavRow(Icons.Outlined.Archive, "Archived coupons", onClick = onNavigateToArchive)
                }
            }
            item { SectionCaption("SMS import", Modifier.padding(top = 14.dp)) }
            item {
                GroupedList {
                    NavRow(Icons.Outlined.AutoAwesome, "AI coupon detection", onClick = onNavigateToAiSettings)
                    GroupDivider()
                    NavRow(Icons.Outlined.Sms, "Trusted senders", onClick = onManageWhitelist)
                }
            }
            item { SectionCaption("Data", Modifier.padding(top = 14.dp)) }
            item {
                GroupedList {
                    NavRow(Icons.Outlined.Storage, "Backup & database", onClick = onNavigateToDatabaseSettings)
                }
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
                currentCurrencyCode = currentCurrency,
                onCurrencySelected = { viewModel.saveSelectedCurrency(it.code) },
                onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun NavRow(icon: ImageVector, title: String, value: String? = null, onClick: () -> Unit) {
    GroupRow(onClick = onClick) {
        RowIcon(icon)
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.extendedColors.inkTertiary)
    }
}
