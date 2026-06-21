package com.oh.shoot.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentCream,
    secondary = AccentGold,
    tertiary = Success,
    background = Background,
    surface = Surface,
    onPrimary = Surface,
    onSecondary = Surface,
    onTertiary = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = Error,
    onError = TextPrimary
)

private val LightColorScheme = darkColorScheme( // Reusing darkColorScheme for consistent contrast defaults
    primary = AccentGold,
    secondary = AccentCream,
    tertiary = Success,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onTertiary = LightSurface,
    onBackground = LightText,
    onSurface = LightText,
    error = Error,
    onError = LightSurface
)

private val PinkColorScheme = darkColorScheme(
    primary = PinkText,
    secondary = AccentGold,
    tertiary = Success,
    background = PinkBackground,
    surface = PinkSurface,
    onPrimary = PinkSurface,
    onSecondary = PinkSurface,
    onTertiary = PinkSurface,
    onBackground = PinkText,
    onSurface = PinkText,
    error = Error,
    onError = PinkSurface
)

private val BlueColorScheme = darkColorScheme(
    primary = BlueText,
    secondary = AccentGold,
    tertiary = Success,
    background = BlueBackground,
    surface = BlueSurface,
    onPrimary = BlueSurface,
    onSecondary = BlueSurface,
    onTertiary = BlueSurface,
    onBackground = BlueText,
    onSurface = BlueText,
    error = Error,
    onError = BlueSurface
)

@Composable
fun OhShootTheme(
    themeName: String = "Dark",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Light" -> LightColorScheme
        "Pink" -> PinkColorScheme
        "Midnight Blue" -> BlueColorScheme
        else -> DarkColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
