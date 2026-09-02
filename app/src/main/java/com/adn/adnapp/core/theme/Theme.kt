package com.adn.adnapp.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = AdnColors.Primary, onPrimary = AdnColors.OnPrimary,
    primaryContainer = AdnColors.Green80, secondary = AdnColors.Secondary,
    background = AdnColors.Background, surface = AdnColors.Surface,
    onBackground = AdnColors.OnBackground, onSurface = AdnColors.OnSurface,
    error = AdnColors.Error
)

private val DarkColorScheme = darkColorScheme(
    primary = AdnColors.Green80, onPrimary = AdnColors.Green20,
    primaryContainer = AdnColors.Green40, secondary = AdnColors.Green60,
    background = AdnColors.Neutral20, surface = AdnColors.Neutral40,
    onBackground = AdnColors.Neutral100, onSurface = AdnColors.Neutral100,
    error = AdnColors.MacroLow
)

@Composable
fun AdnTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
