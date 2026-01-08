package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCouponDialog(
        categoryViewModel: CategoryViewModel,
        couponViewModel: CouponViewModel,
        onAddCoupon: (String, Double, Long, Long?, String?, String?, Boolean, () -> Unit) -> Unit,
        onDismiss: () -> Unit,
        onAddCategory: () -> Unit
) {
    val categories by categoryViewModel.allCategories.collectAsState()
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var isOneTime by remember { mutableStateOf(false) }
    var expiration by remember { mutableStateOf<Long?>(null) }
    var redeemCode by remember { mutableStateOf<String?>("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var creationMessage by remember { mutableStateOf<String?>(null) }

    val parsedCoupon by couponViewModel.parsedCoupon.collectAsState()
    val isLoading by couponViewModel.isLoading.collectAsState()
    val error by couponViewModel.error.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(parsedCoupon) {
        parsedCoupon?.let { coupon ->
            name = coupon.storeName ?: name
            value = coupon.initialValue?.toString() ?: value
            redeemCode = coupon.redeemCode ?: redeemCode
            coupon.expirationDate?.let {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                expiration = sdf.parse(it)?.time
            }
            couponViewModel.clearParsedCoupon()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        ElevatedCard(
                shape = RoundedCornerShape(24.dp),
                colors =
                        CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                        ),
                elevation = CardDefaults.elevatedCardElevation(6.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                            Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Add New Coupon", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(24.dp))

                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { couponViewModel.clearError() }) { Text("Dismiss") }
                    }

                    OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Coupon Name") },
                            leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("One-time use coupon")
                        Switch(checked = isOneTime, onCheckedChange = { isOneTime = it })
                    }

                    if (!isOneTime) {
                        OutlinedTextField(
                                value = value,
                                onValueChange = { value = it },
                                label = { Text("Initial Value") },
                                leadingIcon = {
                                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                            value = redeemCode ?: "",
                            onValueChange = { redeemCode = it },
                            label = { Text("Redeem Code") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                    )
                    Box {
                        OutlinedTextField(
                                value =
                                        expiration?.let {
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                    .format(Date(it))
                                        }
                                                ?: "",
                                onValueChange = {},
                                label = { Text("Expiration Date") },
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                                modifier =
                                        Modifier.matchParentSize().clickable {
                                            showDatePicker = true
                                        }
                        )
                    }

                    if (showDatePicker) {
                        DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(
                                            onClick = {
                                                datePickerState.selectedDateMillis?.let { date ->
                                                    val timeZone = TimeZone.getDefault()
                                                    val offset = timeZone.getOffset(date)
                                                    expiration = date + offset
                                                }
                                                showDatePicker = false
                                            }
                                    ) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) {
                                        Text("Cancel")
                                    }
                                }
                        ) {
                            DatePicker(
                                    state = datePickerState,
                                    title = {
                                        Text(
                                                "Select date",
                                                modifier =
                                                        Modifier.padding(
                                                                start = 24.dp,
                                                                end = 12.dp,
                                                                top = 16.dp
                                                        )
                                        )
                                    },
                                    headline = {
                                        datePickerState.selectedDateMillis?.let {
                                            DatePickerDefaults.DatePickerHeadline(
                                                    selectedDateMillis = it,
                                                    displayMode = datePickerState.displayMode,
                                                    dateFormatter =
                                                            DatePickerDefaults.dateFormatter(),
                                                    modifier =
                                                            Modifier.padding(
                                                                    start = 24.dp,
                                                                    end = 12.dp,
                                                                    bottom = 12.dp
                                                            )
                                            )
                                        }
                                    },
                                    showModeToggle = true
                            )
                        }
                    }

                    if (categories.isEmpty()) {
                        TextButton(onClick = onAddCategory) {
                            Text("No categories found. Add a new one?")
                        }
                    } else {
                        ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded },
                        ) {
                            OutlinedTextField(
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    readOnly = true,
                                    value = selectedCategory?.name ?: "None",
                                    onValueChange = {},
                                    label = { Text("Category") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                                expanded = expanded
                                        )
                                    },
                            )
                            ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                            ) {
                                DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = {
                                            selectedCategory = null
                                            expanded = false
                                        },
                                        contentPadding =
                                                ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                                categories.forEach { category ->
                                    DropdownMenuItem(
                                            text = { Text(category.name) },
                                            onClick = {
                                                selectedCategory = category
                                                expanded = false
                                            },
                                            contentPadding =
                                                    ExposedDropdownMenuDefaults.ItemContentPadding,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                            onClick = {
                                expiration?.let { exp ->
                                    val categoryId = selectedCategory?.id
                                    val initialValue =
                                            if (isOneTime) 1.0 else value.toDoubleOrNull() ?: 0.0
                                    onAddCoupon(
                                            name,
                                            initialValue,
                                            exp,
                                            categoryId,
                                            redeemCode,
                                            creationMessage,
                                            isOneTime
                                    ) { onDismiss() }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled =
                                    expiration != null &&
                                            name.isNotBlank() &&
                                            (isOneTime || value.isNotBlank())
                    ) { Text("Save") }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                            onClick = {
                                clipboardManager.getText()?.text?.let { text ->
                                    creationMessage = text
                                    coroutineScope.launch {
                                        couponViewModel.autofillFromClipboard(text)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                    ) { Text("✨ Auto-fill from Clipboard") }
                }
                if (isLoading) {
                    SparkleAnimation(modifier = Modifier.matchParentSize())
                }
            }
        }
    }
}
