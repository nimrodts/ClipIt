package com.nimroddayan.couponmanager.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nimroddayan.couponmanager.MainActivity
import com.nimroddayan.couponmanager.data.DatabaseManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseSettingsScreen(
        onNavigateUp: () -> Unit,
        onResetDatabase: () -> Unit,
) {
    var showResetConfirmationDialog by remember { mutableStateOf(false) }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val databaseManager = remember { DatabaseManager(context) }

    val exportLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
                    onResult = { uri -> uri?.let { databaseManager.exportDatabase(it) } }
            )

    val importLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent(),
                    onResult = { uri ->
                        if (uri != null) {
                            if (databaseManager.importDatabase(uri)) {
                                showImportSuccessDialog = true
                            }
                        }
                    }
            )

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Database Settings") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateUp) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                        titleContentColor = MaterialTheme.colorScheme.primary,
                                        navigationIconContentColor =
                                                MaterialTheme.colorScheme.primary,
                                        actionIconContentColor = MaterialTheme.colorScheme.primary
                                )
                )
            }
    ) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(it)
                                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.padding(16.dp)) {
                ElevatedCard(
                        colors =
                                CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                ),
                        elevation = CardDefaults.elevatedCardElevation(2.dp)
                ) {
                    Column {
                        SettingsItem(
                                icon = Icons.Filled.ArrowUpward,
                                title = "Import Database",
                                onClick = { importLauncher.launch("*/*") }
                        )
                        HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        SettingsItem(
                                icon = Icons.Filled.ArrowDownward,
                                title = "Export Database",
                                onClick = { exportLauncher.launch("coupon_database.db") }
                        )
                        HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        SettingsItem(
                                icon = Icons.Filled.Delete,
                                title = "Reset Database",
                                onClick = { showResetConfirmationDialog = true }
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirmationDialog) {
        ConfirmationDialog(
                onConfirm = onResetDatabase,
                onDismiss = { showResetConfirmationDialog = false },
                title = "Reset Database",
                message =
                        "Are you sure you want to reset the database? This will permanently delete all your coupons and categories."
        )
    }

    if (showImportSuccessDialog) {
        ConfirmationDialog(
                onConfirm = { restartApp(context) },
                onDismiss = { restartApp(context) },
                title = "Import Successful",
                message = "The database has been imported successfully. The app will now restart."
        )
    }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    (context as? Activity)?.finish()
}

@Composable
private fun SettingsItem(
        icon: ImageVector,
        title: String,
        onClick: () -> Unit,
) {
    Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
                text = title,
                modifier = Modifier.padding(start = 16.dp).weight(1f),
                color = MaterialTheme.colorScheme.onSurface
        )
        Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
