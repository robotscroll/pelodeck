package com.pelodeck.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pelodeck.domain.model.ThemeSkin

@Immutable
data class PeloDeckColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val digitalDisplay: Color,
    val digitalGlow: Color,
    val panelBorder: Color,
    val buttonBackground: Color,
    val buttonText: Color,
    val barBackground: Color,
    val isRetroLed: Boolean = true
)

val WinampColors = PeloDeckColors(
    background = Color(0xFF1B1E23),
    surface = Color(0xFF252830),
    surfaceVariant = Color(0xFF2E323D),
    primary = Color(0xFF00FF44),
    secondary = Color(0xFFFFB000),
    accent = Color(0xFF00D2FF),
    textPrimary = Color(0xFFE8ECEF),
    textSecondary = Color(0xFF9AA5B1),
    digitalDisplay = Color(0xFF00FF44),
    digitalGlow = Color(0x3300FF44),
    panelBorder = Color(0xFF3E4654),
    buttonBackground = Color(0xFF363C4A),
    buttonText = Color(0xFF00FF44),
    barBackground = Color(0xFF131519),
    isRetroLed = true
)

val CyberpunkColors = PeloDeckColors(
    background = Color(0xFF0B0C10),
    surface = Color(0xFF151922),
    surfaceVariant = Color(0xFF1E2330),
    primary = Color(0xFFFFE600),
    secondary = Color(0xFFFF007F),
    accent = Color(0xFF00F0FF),
    textPrimary = Color(0xFFF5F6F8),
    textSecondary = Color(0xFFA0A5B5),
    digitalDisplay = Color(0xFFFFE600),
    digitalGlow = Color(0x44FFE600),
    panelBorder = Color(0xFF00F0FF),
    buttonBackground = Color(0xFF2B1038),
    buttonText = Color(0xFFFFE600),
    barBackground = Color(0xFF07080B),
    isRetroLed = true
)

val MinimalColors = PeloDeckColors(
    background = Color(0xFF121417),
    surface = Color(0xFF1B1E24),
    surfaceVariant = Color(0xFF242932),
    primary = Color(0xFFFFFFFF),
    secondary = Color(0xFF94A3B8),
    accent = Color(0xFF38BDF8),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    digitalDisplay = Color(0xFFF8FAFC),
    digitalGlow = Color(0x00000000),
    panelBorder = Color(0xFF334155),
    buttonBackground = Color(0xFF334155),
    buttonText = Color(0xFFFFFFFF),
    barBackground = Color(0xFF0F172A),
    isRetroLed = false
)

val MatrixColors = PeloDeckColors(
    background = Color(0xFF050805),
    surface = Color(0xFF0A120A),
    surfaceVariant = Color(0xFF0E1A0E),
    primary = Color(0xFF00FF66),
    secondary = Color(0xFF008F11),
    accent = Color(0xFF55FF55),
    textPrimary = Color(0xFF00FF66),
    textSecondary = Color(0xFF009933),
    digitalDisplay = Color(0xFF00FF66),
    digitalGlow = Color(0x4400FF66),
    panelBorder = Color(0xFF008F11),
    buttonBackground = Color(0xFF0D200D),
    buttonText = Color(0xFF00FF66),
    barBackground = Color(0xFF020402),
    isRetroLed = true
)

val CinnamorollColors = PeloDeckColors(
    background = Color(0xFF132338),
    surface = Color(0xFF1E3550),
    surfaceVariant = Color(0xFF284668),
    primary = Color(0xFF89CFF0),      // Iconic Sanrio Baby Blue
    secondary = Color(0xFFFFB7CE),    // Soft Pink Blush
    accent = Color(0xFFF0F9FF),       // Fluffy Cloud White
    textPrimary = Color(0xFFFFFFFF),  // Pure White
    textSecondary = Color(0xFFB0D4ED),// Soft Powder Blue
    digitalDisplay = Color(0xFF89CFF0),
    digitalGlow = Color(0x5589CFF0),
    panelBorder = Color(0xFF89CFF0),
    buttonBackground = Color(0xFF254B73),
    buttonText = Color(0xFFFFFFFF),
    barBackground = Color(0xFF0D1929),
    isRetroLed = false
)

fun getColorsForSkin(skin: ThemeSkin): PeloDeckColors = when (skin) {
    ThemeSkin.WINAMP -> WinampColors
    ThemeSkin.CYBERPUNK -> CyberpunkColors
    ThemeSkin.MINIMAL -> MinimalColors
    ThemeSkin.MATRIX -> MatrixColors
    ThemeSkin.CINNAMOROLL -> CinnamorollColors
}

val LocalPeloDeckColors = staticCompositionLocalOf { WinampColors }
