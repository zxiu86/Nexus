package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private data class AccentColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color
)

fun getAppColorScheme(
    isDark: Boolean,
    backgroundStyle: Int, // 0: Default, 1: AMOLED Pure Black, 2: Pure White
    accentColor: Int // 0: Default (Gold/Orange), 1: Blue, 2: Red
): ColorScheme {
    val (primary, onPrimary, primaryContainer, onPrimaryContainer, secondary, secondaryContainer) = when (accentColor) {
        1 -> AccentColors(
            NexusBlueLight, Color.White, NexusBlueDark, Color.White,
            NexusBluePrimary, NexusBlueContainer
        )
        2 -> AccentColors(
            NexusRedLight, Color.White, NexusRedDark, Color.White,
            NexusRedPrimary, NexusRedContainer
        )
        else -> AccentColors(
            NexusGoldLight, BackgroundDark, NexusGoldDark, TextPrimary,
            NexusOrange, NexusOrangeDark
        )
    }

    return if (isDark) {
        val (bg, surf, surfVar, surfElev) = when (backgroundStyle) {
            1 -> listOf(BackgroundAmoled, SurfaceAmoled, SurfaceVariantAmoled, SurfaceElevatedAmoled)
            else -> listOf(BackgroundDark, SurfaceDark, SurfaceVariantDark, SurfaceElevated)
        }

        darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = TextPrimary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = TextPrimary,
            tertiary = NexusGold,
            onTertiary = bg,
            background = bg,
            onBackground = TextPrimary,
            surface = surf,
            onSurface = TextPrimary,
            surfaceVariant = surfVar,
            onSurfaceVariant = TextSecondary,
            outline = surfElev
        )
    } else {
        lightColorScheme(
            primary = if (accentColor == 0) NexusOrange else primary,
            onPrimary = Color.White,
            primaryContainer = if (accentColor == 0) NexusGold.copy(alpha = 0.25f) else primaryContainer.copy(alpha = 0.2f),
            onPrimaryContainer = TextPrimaryLight,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondaryContainer.copy(alpha = 0.2f),
            onSecondaryContainer = TextPrimaryLight,
            tertiary = NexusOrange,
            onTertiary = Color.White,
            background = if (backgroundStyle == 2) Color.White else BackgroundLight,
            onBackground = TextPrimaryLight,
            surface = SurfaceLight,
            onSurface = TextPrimaryLight,
            surfaceVariant = SurfaceVariantLight,
            onSurfaceVariant = TextSecondaryLight,
            outline = SurfaceElevatedLight
        )
    }
}

@Composable
fun NexusTheme(
    themeMode: Int = 0, // 0: System, 1: Dark, 2: Light
    backgroundStyle: Int = 0, // 0: Default, 1: AMOLED Black, 2: Pure White
    accentColor: Int = 0, // 0: Default, 1: Blue, 2: Red
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        1 -> true
        2 -> false
        else -> darkTheme
    }

    val colorScheme = getAppColorScheme(
        isDark = isDark,
        backgroundStyle = backgroundStyle,
        accentColor = accentColor
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
