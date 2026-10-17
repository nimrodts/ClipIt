package com.nimroddayan.clipit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Every Material role is set explicitly so nothing falls back to the baseline purple.
private val LightColorScheme =
        lightColorScheme(
                primary = Accent,
                onPrimary = Color.White,
                primaryContainer = AccentSoft,
                onPrimaryContainer = OnAccentSoft,
                inversePrimary = AccentDark,
                secondary = Ink,
                onSecondary = Card,
                secondaryContainer = Fill,
                onSecondaryContainer = Ink,
                tertiary = Slate,
                onTertiary = Color.White,
                tertiaryContainer = SlateSoft,
                onTertiaryContainer = Ink,
                background = Ground,
                onBackground = Ink,
                surface = Card,
                onSurface = Ink,
                surfaceVariant = Fill,
                onSurfaceVariant = InkSecondary,
                surfaceTint = Color.Transparent,
                inverseSurface = Color(0xFF1E2120),
                inverseOnSurface = Color(0xFFF3F5F4),
                error = Critical,
                onError = Color.White,
                errorContainer = CriticalSoft,
                onErrorContainer = Color(0xFF5E1610),
                outline = InkTertiary,
                outlineVariant = Hairline,
                scrim = Color.Black,
                surfaceBright = Card,
                surfaceDim = Ground,
                surfaceContainerLowest = Card,
                surfaceContainerLow = Card,
                surfaceContainer = Card,
                surfaceContainerHigh = Card,
                surfaceContainerHighest = Fill,
        )

private val DarkColorScheme =
        darkColorScheme(
                primary = AccentDark,
                onPrimary = OnAccentDark,
                primaryContainer = AccentSoftDark,
                onPrimaryContainer = OnAccentSoftDark,
                inversePrimary = Accent,
                secondary = InkDark,
                onSecondary = GroundDark,
                secondaryContainer = FillDark,
                onSecondaryContainer = InkDark,
                tertiary = SlateDark,
                onTertiary = GroundDark,
                tertiaryContainer = SlateSoftDark,
                onTertiaryContainer = InkDark,
                background = GroundDark,
                onBackground = InkDark,
                surface = CardDark,
                onSurface = InkDark,
                surfaceVariant = FillDark,
                onSurfaceVariant = InkSecondaryDark,
                surfaceTint = Color.Transparent,
                inverseSurface = Color(0xFFEDF0EE),
                inverseOnSurface = Ink,
                error = CriticalDark,
                onError = GroundDark,
                errorContainer = CriticalSoftDark,
                onErrorContainer = Color(0xFFFFC9C2),
                outline = InkTertiaryDark,
                outlineVariant = HairlineDark,
                scrim = Color.Black,
                surfaceBright = RaisedDark,
                surfaceDim = GroundDark,
                surfaceContainerLowest = GroundDark,
                surfaceContainerLow = CardDark,
                surfaceContainer = SheetDark,
                surfaceContainerHigh = SheetDark,
                surfaceContainerHighest = FillDark,
        )

/** Colors the Material scheme has no role for. Read them with `MaterialTheme.extendedColors`. */
@Immutable
data class ExtendedColors(
        val card: Color,
        val sheet: Color,
        val fill: Color,
        val fillStrong: Color,
        /** A control sitting on a `fill` block: lighter than the block in both themes. */
        val chip: Color,
        val inkTertiary: Color,
        val hairline: Color,
        val warn: Color,
        val warnContainer: Color,
        val archive: Color,
        val bar: Color,
        val pass: Color,
        val passInk: Color,
        val passInkSecondary: Color,
        val passFill: Color,
)

private val LightExtended =
        ExtendedColors(
                card = Card,
                sheet = Card,
                fill = Fill,
                fillStrong = FillStrong,
                chip = Card,
                inkTertiary = InkTertiary,
                hairline = Hairline,
                warn = Warn,
                warnContainer = WarnSoft,
                archive = Slate,
                bar = Color(0xF5F7F7F4),
                pass = PassBackground,
                passInk = PassInk,
                passInkSecondary = PassInkSecondary,
                passFill = PassFill,
        )

private val DarkExtended =
        ExtendedColors(
                card = CardDark,
                sheet = SheetDark,
                fill = FillDark,
                fillStrong = FillStrongDark,
                chip = ChipDark,
                inkTertiary = InkTertiaryDark,
                hairline = HairlineDark,
                warn = WarnDark,
                warnContainer = WarnSoftDark,
                archive = SlateDark,
                bar = Color(0xF2181A19),
                pass = PassBackground,
                passInk = PassInk,
                passInkSecondary = PassInkSecondary,
                passFill = PassFill,
        )

private val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

val MaterialTheme.extendedColors: ExtendedColors
    @Composable get() = LocalExtendedColors.current

@Composable
fun ClipItTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extended = if (darkTheme) DarkExtended else LightExtended

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

    CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                shapes = Shapes,
                content = content
        )
    }
}
