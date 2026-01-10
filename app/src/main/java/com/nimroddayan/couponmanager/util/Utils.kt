package com.nimroddayan.couponmanager.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

val availableIcons =
        mapOf(
                "Clothes" to Icons.Filled.Checkroom,
                "Fast Food" to Icons.Filled.Fastfood,
                "Grocery" to Icons.Filled.LocalGroceryStore,
                "Movie" to Icons.Filled.Movie,
                "Theater" to Icons.Filled.Theaters,
                "Restaurant" to Icons.Filled.Restaurant,
                "Cafe" to Icons.Filled.LocalCafe,
                "Bar" to Icons.Filled.LocalBar,
                "Gas Station" to Icons.Filled.LocalGasStation,
                "Flight" to Icons.Filled.Flight,
                "Hotel" to Icons.Filled.Hotel,
                "Gift Card" to Icons.Filled.CardGiftcard,
        )

fun getIconByName(name: String): ImageVector {
    return availableIcons[name] ?: Icons.AutoMirrored.Filled.Help // Default icon
}

fun getContrastColor(hexColor: String): Color {
    return try {
        val color = android.graphics.Color.parseColor(hexColor)
        // Calculate luminance: 0.299*R + 0.587*G + 0.114*B
        val luminance =
                (0.299 * android.graphics.Color.red(color) +
                        0.587 * android.graphics.Color.green(color) +
                        0.114 * android.graphics.Color.blue(color)) / 255
        // Return Black for light backgrounds, White for dark backgrounds
        if (luminance > 0.5) Color.Black else Color.White
    } catch (e: Exception) {
        Color.White
    }
}
