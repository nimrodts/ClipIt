package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nimroddayan.couponmanager.util.availableIcons
import com.nimroddayan.couponmanager.util.getIconByName

val availableColors =
        listOf(
                Color(0xFFF44336),
                Color(0xFFE91E63),
                Color(0xFF9C27B0),
                Color(0xFF673AB7),
                Color(0xFF3F51B5),
                Color(0xFF2196F3),
                Color(0xFF03A9F4),
                Color(0xFF00BCD4),
                Color(0xFF009688),
                Color(0xFF4CAF50),
                Color(0xFF8BC34A),
                Color(0xFFCDDC39),
                Color(0xFFFFEB3B),
                Color(0xFFFFC107),
                Color(0xFFFF9800),
                Color(0xFFFF5722),
                Color(0xFF795548),
                Color(0xFF9E9E9E),
                Color(0xFF607D8B),
                Color(0xFF000000)
        )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(onAddCategory: (String, String, String) -> Unit, onDismiss: () -> Unit) {
        var name by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(availableColors.first()) }
        var selectedIconName by remember { mutableStateOf(availableIcons.keys.first()) }
        val iconList = remember { availableIcons.keys.toList() }

        Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
                ElevatedCard(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors =
                                CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                ),
                        elevation = CardDefaults.elevatedCardElevation(6.dp)
                ) {
                        Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                                Text(
                                        text = "Add New Category",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                )

                                OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { Text("Category Name") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        leadingIcon = {
                                                Icon(
                                                        imageVector = Icons.Default.Abc,
                                                        contentDescription = null
                                                )
                                        }
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                        text = "Select Color",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                        items(availableColors) { color ->
                                                Box(
                                                        modifier =
                                                                Modifier.size(42.dp)
                                                                        .clip(CircleShape)
                                                                        .background(color)
                                                                        .clickable {
                                                                                selectedColor =
                                                                                        color
                                                                        }
                                                                        .then(
                                                                                if (selectedColor ==
                                                                                                color
                                                                                ) {
                                                                                        Modifier.border(
                                                                                                3.dp,
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .primary,
                                                                                                CircleShape
                                                                                        )
                                                                                } else Modifier
                                                                        ),
                                                        contentAlignment = Alignment.Center
                                                ) {
                                                        if (selectedColor == color) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default.Check,
                                                                        contentDescription =
                                                                                "Selected",
                                                                        tint = Color.White,
                                                                        modifier =
                                                                                Modifier.size(24.dp)
                                                                )
                                                        }
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                        text = "Select Icon",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.Start)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(180.dp)
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .surfaceContainerLow,
                                                                RoundedCornerShape(12.dp)
                                                        )
                                                        .border(
                                                                1.dp,
                                                                MaterialTheme.colorScheme
                                                                        .outlineVariant,
                                                                RoundedCornerShape(12.dp)
                                                        )
                                ) {
                                        LazyVerticalGrid(
                                                columns = GridCells.Adaptive(minSize = 56.dp),
                                                contentPadding = PaddingValues(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxSize()
                                        ) {
                                                items(iconList) { iconName ->
                                                        val isSelected =
                                                                selectedIconName == iconName
                                                        Box(
                                                                modifier =
                                                                        Modifier.aspectRatio(1f)
                                                                                .clip(
                                                                                        RoundedCornerShape(
                                                                                                12.dp
                                                                                        )
                                                                                )
                                                                                .background(
                                                                                        if (isSelected
                                                                                        )
                                                                                                selectedColor
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.2f
                                                                                                        )
                                                                                        else
                                                                                                MaterialTheme
                                                                                                        .colorScheme
                                                                                                        .surface
                                                                                )
                                                                                .clickable {
                                                                                        selectedIconName =
                                                                                                iconName
                                                                                }
                                                                                .border(
                                                                                        width =
                                                                                                if (isSelected
                                                                                                )
                                                                                                        2.dp
                                                                                                else
                                                                                                        1.dp,
                                                                                        color =
                                                                                                if (isSelected
                                                                                                )
                                                                                                        selectedColor
                                                                                                else
                                                                                                        MaterialTheme
                                                                                                                .colorScheme
                                                                                                                .outlineVariant,
                                                                                        shape =
                                                                                                RoundedCornerShape(
                                                                                                        12.dp
                                                                                                )
                                                                                ),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                getIconByName(
                                                                                        iconName
                                                                                ),
                                                                        contentDescription =
                                                                                iconName,
                                                                        tint =
                                                                                if (isSelected)
                                                                                        selectedColor
                                                                                else
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurfaceVariant,
                                                                        modifier =
                                                                                Modifier.size(28.dp)
                                                                )
                                                        }
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                ) {
                                        TextButton(
                                                onClick = onDismiss,
                                                modifier = Modifier.padding(end = 8.dp)
                                        ) { Text("Cancel") }
                                        Button(
                                                onClick = {
                                                        if (name.isNotBlank()) {
                                                                val colorHex =
                                                                        "#" +
                                                                                Integer.toHexString(
                                                                                                selectedColor
                                                                                                        .toArgb()
                                                                                        )
                                                                                        .substring(
                                                                                                2
                                                                                        )
                                                                onAddCategory(
                                                                        name,
                                                                        colorHex,
                                                                        selectedIconName
                                                                )
                                                                onDismiss()
                                                        }
                                                },
                                                enabled = name.isNotBlank()
                                        ) { Text("Save Category") }
                                }
                        }
                }
        }
}
