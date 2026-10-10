package com.nimroddayan.clipit.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

// System font with size-specific tracking: large text is tightened, small labels are opened up.
private val System = FontFamily.Default

val Typography =
        Typography(
                // Balance display (sheet, Insights hero). Tabular so digits don't jump as they change.
                displayLarge =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Bold,
                                fontSize = 46.sp,
                                lineHeight = 48.sp,
                                letterSpacing = (-0.032).em,
                                fontFeatureSettings = "tnum",
                        ),
                // Large screen title ("Coupons").
                displayMedium =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp,
                                lineHeight = 38.sp,
                                letterSpacing = (-0.024).em,
                        ),
                displaySmall =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                lineHeight = 32.sp,
                                letterSpacing = (-0.02).em,
                        ),
                headlineLarge =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                lineHeight = 32.sp,
                                letterSpacing = (-0.02).em,
                        ),
                headlineMedium =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                lineHeight = 30.sp,
                                letterSpacing = (-0.02).em,
                        ),
                // Sheet and dialog titles.
                headlineSmall =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                lineHeight = 28.sp,
                                letterSpacing = (-0.02).em,
                        ),
                titleLarge =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                lineHeight = 25.sp,
                                letterSpacing = (-0.015).em,
                        ),
                // Card name.
                titleMedium =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.5.sp,
                                lineHeight = 22.sp,
                                letterSpacing = (-0.012).em,
                        ),
                titleSmall =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                letterSpacing = (-0.005).em,
                        ),
                bodyLarge =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                letterSpacing = 0.em,
                        ),
                bodyMedium =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.5.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.em,
                        ),
                // Card meta line.
                bodySmall =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                letterSpacing = 0.005.em,
                        ),
                // Buttons.
                labelLarge =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.em,
                        ),
                labelMedium =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 0.01.em,
                        ),
                // Section captions (set uppercase at the call site) and tab labels.
                labelSmall =
                        TextStyle(
                                fontFamily = System,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 0.06.em,
                        ),
        )

/** Money amounts in lists and rows: tabular figures so columns of digits line up. */
val TextStyle.tabular: TextStyle
    get() = copy(fontFeatureSettings = "tnum")

val Shapes =
        Shapes(
                extraSmall = RoundedCornerShape(6.dp),
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(18.dp),
                extraLarge = RoundedCornerShape(28.dp),
        )
