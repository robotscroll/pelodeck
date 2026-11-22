package com.pelodeck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.pelodeck.domain.model.ThemeSkin

object PeloDeckTheme {
    val colors: PeloDeckColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPeloDeckColors.current
}

@Composable
fun PeloDeckTheme(
    skin: ThemeSkin = ThemeSkin.WINAMP,
    content: @Composable () -> Unit
) {
    val peloColors = getColorsForSkin(skin)

    val materialColors = darkColorScheme(
        primary = peloColors.primary,
        secondary = peloColors.secondary,
        tertiary = peloColors.accent,
        background = peloColors.background,
        surface = peloColors.surface,
        onPrimary = peloColors.background,
        onSecondary = peloColors.background,
        onBackground = peloColors.textPrimary,
        onSurface = peloColors.textPrimary
    )

    CompositionLocalProvider(
        LocalPeloDeckColors provides peloColors
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
            content = content
        )
    }
}
