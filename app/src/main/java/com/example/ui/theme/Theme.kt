package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondary = SageSecondary,
    onSecondary = SageOnSecondary,
    secondaryContainer = SageSecondaryContainer,
    onSecondaryContainer = SageOnSecondaryContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = AmberOnTertiaryContainer,
    background = IvoryBackground,
    onBackground = IvoryOnBackground,
    surface = IvorySurface,
    onSurface = IvoryOnSurface,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = IvoryOnSurfaceVariant,
    outline = IvoryOutline,
    outlineVariant = IvoryOutlineVariant,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = DarkEmeraldPrimary,
    onPrimary = DarkEmeraldOnPrimary,
    primaryContainer = DarkEmeraldPrimaryContainer,
    onPrimaryContainer = DarkEmeraldOnPrimaryContainer,
    secondary = DarkSageSecondary,
    onSecondary = DarkSageOnSecondary,
    secondaryContainer = DarkSageSecondaryContainer,
    onSecondaryContainer = DarkSageOnSecondaryContainer,
    tertiary = DarkAmberTertiary,
    onTertiary = DarkAmberOnTertiary,
    tertiaryContainer = DarkAmberTertiaryContainer,
    onTertiaryContainer = DarkAmberOnTertiaryContainer,
    background = DarkForestBackground,
    onBackground = DarkForestOnBackground,
    surface = DarkForestSurface,
    onSurface = DarkForestOnSurface,
    surfaceVariant = DarkForestSurfaceVariant,
    onSurfaceVariant = DarkForestOnSurfaceVariant,
    outline = DarkForestOutline,
    outlineVariant = DarkForestOutlineVariant,
    error = DarkErrorRed
)

val KasHarianShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun KasHarianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KasHarianTypography,
        shapes = KasHarianShapes,
        content = content
    )
}
