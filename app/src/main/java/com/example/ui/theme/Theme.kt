package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BloomPrimary,
    onPrimary = Color.White,
    primaryContainer = BloomSoftLavender,
    onPrimaryContainer = BloomPrimary,
    secondary = BloomSecondary,
    onSecondary = Color.White,
    secondaryContainer = BloomSoftLavender,
    onSecondaryContainer = BloomPrimary,
    tertiary = BloomSuccess,
    onTertiary = Color.White,
    background = BloomBackground,
    onBackground = BloomTextPrimary,
    surface = BloomSurface,
    onSurface = BloomTextPrimary,
    surfaceVariant = BloomSurfaceVariant,
    onSurfaceVariant = BloomTextSecondary,
    outline = BloomDivider,
    outlineVariant = BloomTextTertiary,
    error = BloomError,
    onError = Color.White,
    errorContainer = BloomErrorLight,
    onErrorContainer = BloomError
)

private val DarkColorScheme = darkColorScheme(
    primary = BloomSecondary,
    onPrimary = BloomDarkBackground,
    primaryContainer = BloomDarkSurfaceVariant,
    onPrimaryContainer = BloomSecondary,
    secondary = BloomPrimary,
    onSecondary = Color.White,
    secondaryContainer = BloomDarkSurfaceVariant,
    onSecondaryContainer = BloomSecondary,
    tertiary = BloomSuccess,
    onTertiary = BloomDarkBackground,
    background = BloomDarkBackground,
    onBackground = BloomDarkTextPrimary,
    surface = BloomDarkSurface,
    onSurface = BloomDarkTextPrimary,
    surfaceVariant = BloomDarkSurfaceVariant,
    onSurfaceVariant = BloomDarkTextSecondary,
    outline = BloomDarkDivider,
    outlineVariant = BloomDarkTextSecondary,
    error = BloomError,
    onError = Color.White,
    errorContainer = Color(0xFF4A1E24),
    onErrorContainer = BloomError
)

@Composable
fun BloomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
