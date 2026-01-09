package com.nimroddayan.couponmanager.ui.screen

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RedeemCodeDialog(
        redeemCode: String,
        onDismiss: () -> Unit,
) {
        val clipboardManager = LocalClipboardManager.current
        val totalPages = 10_000
        val startIndex = totalPages / 2
        val initialPage = if (startIndex % 2 == 0) startIndex else startIndex + 1
        val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { totalPages })

        var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(redeemCode) {
                withContext(Dispatchers.Default) {
                        qrBitmap = generateBarcode(redeemCode, BarcodeFormat.QR_CODE, 200, 200)
                        barcodeBitmap =
                                generateBarcode(redeemCode, BarcodeFormat.CODE_128, 300, 100)
                }
        }

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

                                HorizontalPager(state = pagerState) { page ->
                                        Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.fillMaxWidth()
                                        ) {
                                                if (page % 2 == 0) {
                                                        Text(
                                                                text = "QR Code",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelLarge,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        BarcodeImageDisplay(
                                                                bitmap = qrBitmap,
                                                                width = 200.dp,
                                                                height = 200.dp
                                                        )
                                                } else {
                                                        Text(
                                                                text = "Barcode",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .labelLarge,
                                                                color =
                                                                        MaterialTheme.colorScheme
                                                                                .primary
                                                        )
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Box(
                                                                modifier = Modifier.height(200.dp),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                BarcodeImageDisplay(
                                                                        bitmap = barcodeBitmap,
                                                                        width = 300.dp,
                                                                        height = 100.dp
                                                                )
                                                        }
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Pager Indicators
                                Row(
                                        Modifier.height(20.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                ) {
                                        repeat(2) { iteration ->
                                                val color =
                                                        if (pagerState.currentPage % 2 == iteration)
                                                                MaterialTheme.colorScheme.primary
                                                        else
                                                                MaterialTheme.colorScheme
                                                                        .surfaceVariant
                                                Box(
                                                        modifier =
                                                                Modifier.padding(2.dp)
                                                                        .clip(CircleShape)
                                                                        .background(color)
                                                                        .size(8.dp)
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

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
                                                                vertical = 16.dp,
                                                                horizontal = 16.dp
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        Text(
                                                text = redeemCode,
                                                fontSize = 24.sp,
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

                                Spacer(modifier = Modifier.height(24.dp))

                                TextButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.fillMaxWidth()
                                ) { Text("Close") }
                        }
                }
        }
}

@Composable
fun BarcodeImageDisplay(bitmap: Bitmap?, width: Dp, height: Dp) {
        bitmap?.let { bmp ->
                val painter = remember(bmp) { BitmapPainter(bmp.asImageBitmap()) }
                Image(
                        painter = painter,
                        contentDescription = "Barcode",
                        modifier =
                                Modifier.size(width = width, height = height)
                                        .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                )
        }
                ?: Box(
                        modifier =
                                Modifier.size(width = width, height = height)
                                        .background(Color.LightGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                ) { Text("Generating...", style = MaterialTheme.typography.bodySmall) }
}

private fun generateBarcode(
        content: String,
        format: BarcodeFormat,
        width: Int,
        height: Int
): Bitmap? {
        println("DEBUG: Generating barcode for $content format $format")
        return try {
                val writer = MultiFormatWriter()
                val bitMatrix: BitMatrix = writer.encode(content, format, width, height)
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                for (x in 0 until width) {
                        for (y in 0 until height) {
                                bmp.setPixel(
                                        x,
                                        y,
                                        if (bitMatrix[x, y]) AndroidColor.BLACK
                                        else AndroidColor.WHITE
                                )
                        }
                }
                bmp
        } catch (e: WriterException) {
                e.printStackTrace()
                null
        } catch (e: Exception) {
                e.printStackTrace()
                null
        }
}
