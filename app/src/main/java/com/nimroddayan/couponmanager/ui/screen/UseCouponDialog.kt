package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nimroddayan.couponmanager.data.model.Coupon
import kotlinx.coroutines.delay

@Composable
fun UseCouponDialog(
    coupon: Coupon,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountToUse by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Use Coupon", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Text("Current Balance: ₪${String.format("%.2f", coupon.currentValue)}")
                OutlinedTextField(
                    value = amountToUse,
                    onValueChange = { amountToUse = it },
                    label = { Text("Amount to use") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val amount = amountToUse.toDoubleOrNull() ?: 0.0
                            onConfirm(amount)
                            onDismiss()
                            keyboardController?.hide()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Button(
                    onClick = {
                        val amount = amountToUse.toDoubleOrNull() ?: 0.0
                        onConfirm(amount)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(100) // A small delay to ensure the UI is ready for focus
        focusRequester.requestFocus()
    }
}
