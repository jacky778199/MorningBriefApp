package com.morningbrief.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SunrisePrimary,
    secondary = SunriseSecondary,
    tertiary = SunriseTertiary,
    background = SunriseBackground,
    surface = SunriseSurface,
    surfaceVariant = SunriseSurfaceVariant,
    onPrimary = SunriseSurface,
    onSecondary = SunriseSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = SunriseCardBorder,
    outlineVariant = SunriseCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    secondary = NightSecondary,
    tertiary = NightTertiary,
    background = NightBackground,
    surface = NightSurface,
    surfaceVariant = NightSurfaceVariant,
    onPrimary = NightBackground,
    onSecondary = NightBackground,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = NightCardBorder,
    outlineVariant = NightCardBorder
)

@Composable
fun MorningBriefTheme(
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
