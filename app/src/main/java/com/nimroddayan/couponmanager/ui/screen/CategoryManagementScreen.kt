package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.util.getContrastColor
import com.nimroddayan.couponmanager.util.getIconByName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(viewModel: CategoryViewModel, onNavigateUp: () -> Unit) {
        val categories by viewModel.allCategories.collectAsState()
        var showAddCategoryDialog by remember { mutableStateOf(false) }
        var categoryToDelete by remember { mutableStateOf<Category?>(null) }
        var categoryToRename by remember { mutableStateOf<Category?>(null) }

        if (showAddCategoryDialog) {
                AddCategoryDialog(
                        onAddCategory = { name, color, icon ->
                                viewModel.insert(
                                        Category(name = name, colorHex = color, iconName = icon)
                                )
                        },
                        onDismiss = { showAddCategoryDialog = false }
                )
        }

        if (categoryToDelete != null) {
                ConfirmationDialog(
                        title = "Delete Category",
                        message =
                                "Are you sure you want to delete '${categoryToDelete?.name}'? This action cannot be undone.",
                        onConfirm = {
                                categoryToDelete?.let { viewModel.delete(it) }
                                categoryToDelete = null
                        },
                        onDismiss = { categoryToDelete = null }
                )
        }

        if (categoryToRename != null) {
                EditCategoryDialog(
                        category = categoryToRename!!,
                        onConfirm = { newName, newColor, newIcon ->
                                categoryToRename?.let {
                                        viewModel.update(
                                                it.copy(
                                                        name = newName,
                                                        colorHex = newColor,
                                                        iconName = newIcon
                                                )
                                        )
                                }
                                categoryToRename = null
                        },
                        onDismiss = { categoryToRename = null }
                )
        }

        Scaffold(
                topBar = {
                        TopAppBar(
                                title = { Text("Manage Categories") },
                                navigationIcon = {
                                        IconButton(onClick = onNavigateUp) {
                                                Icon(
                                                        imageVector =
                                                                Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                )
                                        }
                                },
                                actions = {
                                        IconButton(onClick = { showAddCategoryDialog = true }) {
                                                Icon(
                                                        Icons.Default.Add,
                                                        contentDescription = "Add Category"
                                                )
                                        }
                                },
                                colors =
                                        TopAppBarDefaults.topAppBarColors(
                                                containerColor =
                                                        MaterialTheme.colorScheme.background,
                                                titleContentColor =
                                                        MaterialTheme.colorScheme.primary,
                                                navigationIconContentColor =
                                                        MaterialTheme.colorScheme.primary,
                                                actionIconContentColor =
                                                        MaterialTheme.colorScheme.primary
                                        )
                        )
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.padding(paddingValues)
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.background)
                ) {
                        LazyColumn(
                                contentPadding =
                                        androidx.compose.foundation.layout.PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                items(categories) { category ->
                                        CategoryItem(
                                                category = category,
                                                onEditClick = { categoryToRename = category },
                                                onDeleteClick = { categoryToDelete = category }
                                        )
                                }
                        }
                }
        }
}

@Composable
fun CategoryItem(
        category: Category,
        onEditClick: () -> Unit,
        onDeleteClick: () -> Unit,
) {
        ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(2.dp),
                colors =
                        CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                        )
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        Box(
                                modifier =
                                        Modifier.size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                        Color(
                                                                android.graphics.Color.parseColor(
                                                                        category.colorHex
                                                                )
                                                        )
                                                )
                        ) {
                                Icon(
                                        imageVector = getIconByName(category.iconName),
                                        contentDescription = category.name,
                                        modifier = Modifier.align(Alignment.Center),
                                        tint = getContrastColor(category.colorHex)
                                )
                        }
                        Text(
                                text = category.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = onEditClick) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename Category")
                        }
                        IconButton(onClick = onDeleteClick) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Category")
                        }
                }
        }
}
