package com.brigadeiro.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BrigadeiroLightColors = lightColorScheme(
    primary = Moss,
    onPrimary = White,
    primaryContainer = MossContainer,
    onPrimaryContainer = OnMossContainer,

    secondary = MossDark,
    onSecondary = White,
    secondaryContainer = MossContainer,
    onSecondaryContainer = OnMossContainer,

    tertiary = Warning,
    onTertiary = White,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,

    background = NeutralBackground,
    onBackground = Ink,
    surface = NeutralSurface,
    onSurface = Ink,
    surfaceVariant = NeutralField,
    onSurfaceVariant = InkSecondary,
    surfaceTint = Moss,

    outline = NeutralBorder,
    outlineVariant = NeutralBorder,

    error = ErrorRed,
    onError = White,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    scrim = Scrim,
)

/**
 * Brigadeiro is intentionally light-only for the MVP; the same clean scheme is
 * returned regardless of the system setting.
 */
@Composable
fun BrigadeiroTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrigadeiroLightColors,
        typography = BrigadeiroTypography,
        shapes = BrigadeiroShapes,
        content = content,
    )
}
