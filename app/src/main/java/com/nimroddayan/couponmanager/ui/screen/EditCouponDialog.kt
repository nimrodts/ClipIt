package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import com.nimroddayan.couponmanager.ui.viewmodel.CouponViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCouponDialog(
        coupon: Coupon,
        categoryViewModel: CategoryViewModel,
        couponViewModel: CouponViewModel,
        onDismiss: () -> Unit,
) {
    val categories by categoryViewModel.allCategories.collectAsState(initial = emptyList())
    var name by remember { mutableStateOf(coupon.name) }
    var value by remember { mutableStateOf(coupon.initialValue.toString()) }
    var currentValue by remember { mutableStateOf(coupon.currentValue.toString()) }
    var expiration by remember { mutableStateOf<Long?>(coupon.expirationDate) }
    var redeemCode by remember { mutableStateOf(coupon.redeemCode) }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by
            remember(coupon, categories) {
                mutableStateOf(categories.find { it.id == coupon.categoryId })
            }
    var showDatePicker by remember { mutableStateOf(false) }
    // DatePicker only supports years 1900-2100. Check if the date is within range.
    val validExpirationDate =
            remember(coupon.expirationDate) {
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = coupon.expirationDate
                val year = calendar.get(java.util.Calendar.YEAR)
                if (year in 1900..2100) coupon.expirationDate else System.currentTimeMillis()
            }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = validExpirationDate)

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Coupon Name") },
                        modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("Initial Value") },
                        modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = currentValue,
                        onValueChange = { currentValue = it },
                        label = { Text("Current Value") },
                        modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                        value = redeemCode ?: "",
                        onValueChange = { redeemCode = it },
                        label = { Text("Redeem Code") },
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
                            modifier = Modifier.fillMaxWidth()
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
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
                                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                            }
                    ) {
                        DatePicker(
                                state = datePickerState,
                        )
                    }
                }

                ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = selectedCategory?.name ?: "",
                            onValueChange = {},
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                    )
                    ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Button(
                        onClick = {
                            expiration?.let { exp ->
                                selectedCategory?.let { category ->
                                    val updatedCoupon =
                                            coupon.copy(
                                                    name = name,
                                                    initialValue = value.toDouble(),
                                                    currentValue = currentValue.toDouble(),
                                                    expirationDate = exp,
                                                    categoryId = category.id,
                                                    redeemCode = redeemCode
                                            )
                                    couponViewModel.update(updatedCoupon)
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedCategory != null && expiration != null
                ) { Text("Save") }
            }
        }
    }
}
