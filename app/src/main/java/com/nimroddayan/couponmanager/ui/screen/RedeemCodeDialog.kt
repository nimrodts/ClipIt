package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun RedeemCodeDialog(
    redeemCode: String,
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = redeemCode,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(redeemCode))
                        onDismiss()
                    },
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    Text("Copy")
                }
            }
        }
    }
}
