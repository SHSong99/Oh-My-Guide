package com.ohmyguide.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = BgWhite,
    secondary = Secondary,
    onSecondary = TextPrimary,
    background = BgWhite,
    onBackground = TextPrimary,
    surface = BgWhite,
    onSurface = TextPrimary,
    surfaceVariant = BgSub,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    outlineVariant = BorderLight,
    error = Error,
)

@Composable
fun OhMyGuideTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}