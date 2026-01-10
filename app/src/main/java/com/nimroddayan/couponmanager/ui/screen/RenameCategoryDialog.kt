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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.util.availableIcons
import com.nimroddayan.couponmanager.util.getIconByName
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryDialog(
        category: Category,
        onConfirm: (String, String, String) -> Unit,
        onDismiss: () -> Unit
) {
        var newName by remember { mutableStateOf(category.name) }
        var selectedIconName by remember { mutableStateOf(category.iconName) }
        val iconList = remember { availableIcons.keys.toList() }
        var selectedColor by remember {
                mutableStateOf(
                        try {
                                Color(android.graphics.Color.parseColor(category.colorHex))
                        } catch (e: Exception) {
                                availableColors.first()
                        }
                )
        }
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

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
                                        text = "Edit Category",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                )

                                OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        label = { Text("Category Name") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions =
                                                KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions =
                                                KeyboardActions(
                                                        onDone = {
                                                                if (newName.isNotBlank()) {
                                                                        val colorHex =
                                                                                "#" +
                                                                                        Integer.toHexString(
                                                                                                        selectedColor
                                                                                                                .toArgb()
                                                                                                )
                                                                                                .substring(
                                                                                                        2
                                                                                                )
                                                                        onConfirm(
                                                                                newName,
                                                                                colorHex,
                                                                                selectedIconName
                                                                        )
                                                                        keyboardController?.hide()
                                                                }
                                                        }
                                                ),
                                        leadingIcon = {
                                                Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = null
                                                )
                                        },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .focusRequester(focusRequester)
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

                                Spacer(modifier = Modifier.padding(24.dp))

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

                                Spacer(modifier = Modifier.padding(24.dp))

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
                                                        if (newName.isNotBlank()) {
                                                                val colorHex =
                                                                        "#" +
                                                                                Integer.toHexString(
                                                                                                selectedColor
                                                                                                        .toArgb()
                                                                                        )
                                                                                        .substring(
                                                                                                2
                                                                                        )
                                                                onConfirm(
                                                                        newName,
                                                                        colorHex,
                                                                        selectedIconName
                                                                )
                                                        }
                                                },
                                                enabled = newName.isNotBlank()
                                        ) { Text("Save") }
                                }
                        }
                }
        }

        LaunchedEffect(Unit) {
                delay(100)
                focusRequester.requestFocus()
        }
}
