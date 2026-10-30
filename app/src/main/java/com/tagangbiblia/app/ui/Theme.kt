package com.tagangbiblia.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import com.tagangbiblia.app.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF6B3F2A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF1DCC4),
    onPrimaryContainer = Color(0xFF3A1D0E),
    secondary = Color(0xFF8A6A2F),
    background = Color(0xFFFBF5EA),
    onBackground = Color(0xFF2A211A),
    surface = Color(0xFFFBF5EA),
    onSurface = Color(0xFF2A211A),
    surfaceVariant = Color(0xFFEFE4D2),
    onSurfaceVariant = Color(0xFF5B4D3F),
    surfaceContainer = Color(0xFFF4EBDA),
    surfaceContainerHigh = Color(0xFFEFE4D2),
    outlineVariant = Color(0xFFD9CBB4),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE2B48F),
    onPrimary = Color(0xFF3A1D0E),
    primaryContainer = Color(0xFF4A3425),
    onPrimaryContainer = Color(0xFFFBE3CC),
    secondary = Color(0xFFD9B873),
    background = Color(0xFF15130F),
    onBackground = Color(0xFFE8E0D2),
    surface = Color(0xFF15130F),
    onSurface = Color(0xFFE8E0D2),
    surfaceVariant = Color(0xFF2A251D),
    onSurfaceVariant = Color(0xFFC4B8A5),
    surfaceContainer = Color(0xFF1E1A14),
    surfaceContainerHigh = Color(0xFF2A251D),
    outlineVariant = Color(0xFF443C30),
)

@Composable
fun BibleTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors, content = content)
}
