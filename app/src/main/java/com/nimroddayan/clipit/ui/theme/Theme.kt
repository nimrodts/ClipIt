package com.nimroddayan.clipit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
        darkColorScheme(
                primary = nord8,
                secondary = nord10,
                background = nord0,
                surface = nord1,
                onPrimary = nord0,
                onSecondary = nord6,
                onBackground = nord6,
                onSurface = nord6,
        )

private val LightColorScheme =
        lightColorScheme(
                primary = nord10,
                secondary = nord8,
                background = nord4, // Use nord4 for a grayish background
                surface = nord6, // Use nord6 for a lighter surface
                onPrimary = nord6,
                onSecondary = nord0,
                onBackground = nord0,
                onSurface = nord0,
        )

@Composable
fun ClipItTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
) {
    val colorScheme =
            if (darkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Edge-to-edge: the app draws behind the system bars, so only the icon contrast is set,
            // and it follows the in-app theme rather than the system one.
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


