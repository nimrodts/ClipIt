package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onManageCategories: () -> Unit,
    onResetDatabase: () -> Unit,
    onNavigateToArchive: () -> Unit
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            SettingsItem(
                icon = Icons.Filled.DarkMode,
                title = "Dark Mode",
                onClick = { onThemeChange(!isDarkTheme) }
            ) {
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeChange
                )
            }
            Divider(modifier = Modifier.padding(start = 56.dp))
            SettingsItem(
                icon = Icons.Filled.Category,
                title = "Manage Categories",
                onClick = onManageCategories
            )
            Divider(modifier = Modifier.padding(start = 56.dp))
            SettingsItem(
                icon = Icons.Filled.Archive,
                title = "Archived Coupons",
                onClick = onNavigateToArchive
            )
            Divider(modifier = Modifier.padding(start = 56.dp))
            SettingsItem(
                icon = Icons.Filled.Delete,
                title = "Reset Database",
                onClick = { showConfirmationDialog = true }
            )
        }
    }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            onConfirm = onResetDatabase,
            onDismiss = { showConfirmationDialog = false },
            title = "Reset Database",
            message = "Are you sure you want to reset the database? This will permanently delete all your coupons and categories."
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurface)
        Text(
            text = title,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (content == null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        } else {
            content()
        }
    }
}
