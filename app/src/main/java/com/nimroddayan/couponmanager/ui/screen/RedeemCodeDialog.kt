package com.nimroddayan.couponmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                        ) {
                                Text(
                                        text = "Redeem Code",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                                MaterialTheme.colorScheme
                                                                        .surfaceContainerHigh
                                                        )
                                                        .clickable {
                                                                clipboardManager.setText(
                                                                        AnnotatedString(redeemCode)
                                                                )
                                                        }
                                                        .padding(
                                                                vertical = 24.dp,
                                                                horizontal = 16.dp
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text = redeemCode,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center,
                                                letterSpacing = 2.sp
                                        )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text = "Tap code to copy",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(32.dp))

                                TextButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.fillMaxWidth()
                                ) { Text("Close") }
                        }
                }
        }
}
