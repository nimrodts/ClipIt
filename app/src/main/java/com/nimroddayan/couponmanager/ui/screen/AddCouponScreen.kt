package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.ui.viewmodel.CategoryViewModel
import java.util.Calendar

@Composable
fun AddCouponDialog(
    categoryViewModel: CategoryViewModel,
    onAddCoupon: (String, Double, Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val categories by categoryViewModel.allCategories.collectAsState()
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var expiration by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Add Coupon", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
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
                    value = expiration,
                    onValueChange = { expiration = it },
                    label = { Text("Expiration Date (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "",
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = { 
                                selectedCategory = category
                                expanded = false
                            })
                        }
                    }
                }
                Button(
                    onClick = {
                        val date = expiration.split("-")
                        if (date.size == 3) {
                            val year = date[0].toInt()
                            val month = date[1].toInt() - 1
                            val day = date[2].toInt()
                            val calendar = Calendar.getInstance()
                            calendar.set(year, month, day)

                            selectedCategory?.let { 
                                onAddCoupon(name, value.toDouble(), calendar.timeInMillis, it.id)
                            }
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
