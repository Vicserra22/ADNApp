package com.adn.adnapp.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.adn.adnapp.domain.model.AppArea

data class AreaPalette(val accent: Color, val soft: Color, val ink: Color)

fun AppArea.palette() = when (this) {
    AppArea.AGENDA -> AreaPalette(Color(0xFF236784), Color(0xFFC5EAFE), Color(0xFF123746))
    AppArea.NUTRITION -> AreaPalette(AdnColors.Primary, AdnColors.Green80, Color(0xFF173C27))
    AppArea.SPORTS -> AreaPalette(Color(0xFF973D4C), Color(0xFFFFD9DD), Color(0xFF501D27))
    AppArea.FINANCE -> AreaPalette(Color(0xFF725B00), Color(0xFFFFE791), Color(0xFF3E3100))
    AppArea.PHILOSOPHY -> AreaPalette(Color(0xFF725194), Color(0xFFEEDCFF), Color(0xFF392550))
}

@Composable
fun AreaTheme(area: AppArea, content: @Composable () -> Unit) {
    val palette = area.palette()
    val dark = isSystemInDarkTheme()
    val scheme = if (dark) darkColorScheme(
        primary = palette.soft, onPrimary = palette.ink,
        primaryContainer = palette.ink, onPrimaryContainer = palette.soft,
        secondary = palette.soft, onSecondary = palette.ink,
        secondaryContainer = palette.ink, onSecondaryContainer = palette.soft,
        background = Color(0xFF17191A), surface = Color(0xFF1E2021)
    ) else lightColorScheme(
        primary = palette.accent, onPrimary = Color.White,
        primaryContainer = palette.soft, onPrimaryContainer = palette.ink,
        secondary = palette.accent, onSecondary = Color.White,
        secondaryContainer = palette.soft, onSecondaryContainer = palette.ink,
        background = if (area == AppArea.NUTRITION) AdnColors.Background else Color(0xFFFAFAFA),
        surface = if (area == AppArea.NUTRITION) AdnColors.Surface else Color(0xFFFCFCFC)
    )
    MaterialTheme(colorScheme = scheme, typography = Typography, content = content)
}

