package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TTMovieHubColorScheme = darkColorScheme(
    primary = BrandRed,
    onPrimary = TextPrimary,
    primaryContainer = BrandRedDark,
    onPrimaryContainer = TextPrimary,
    secondary = BrandGold,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = BrandGold,
    tertiary = AccentCyan,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = StatusError,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TTMovieHubColorScheme,
        typography = Typography,
        content = content
    )
}
