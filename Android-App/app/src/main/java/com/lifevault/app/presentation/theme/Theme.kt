package com.lifevault.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LifeVaultDarkColorScheme = darkColorScheme(
    primary = RoyalPurple,
    onPrimary = TextPrimary,
    secondary = DeepViolet,
    onSecondary = TextPrimary,
    tertiary = SoftPurpleGlow,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = TextPrimary
)

private val LifeVaultLightColorScheme = lightColorScheme(
    primary = RoyalPurple,
    onPrimary = TextPrimary,
    secondary = DeepViolet,
    onSecondary = TextPrimary,
    tertiary = SoftPurpleGlow,
    background = androidx.compose.ui.graphics.Color(0xFFF7F5FA),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1A1A1F),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1A1F),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFEDE9F4),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF5A5A63),
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun LifeVaultTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LifeVaultDarkColorScheme else LifeVaultLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LifeVaultTypography,
        shapes = LifeVaultShapes,
        content = content
    )
}
