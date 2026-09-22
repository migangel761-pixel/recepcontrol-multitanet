package com.aistudio.recepcontrol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Sky600,
    onPrimary = PureWhite,
    primaryContainer = Sky100,
    onPrimaryContainer = Sky700,
    secondary = Emerald600,
    onSecondary = PureWhite,
    secondaryContainer = Emerald100,
    onSecondaryContainer = Emerald800,
    tertiary = Amber600,
    onTertiary = PureWhite,
    tertiaryContainer = Amber100,
    onTertiaryContainer = Amber900,
    error = Rose600,
    onError = PureWhite,
    errorContainer = Rose100,
    onErrorContainer = Rose600,
    background = Slate100,
    onBackground = Slate950,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate300,
    outlineVariant = Slate200
)

@Composable
fun RecepControlTheme(
    darkTheme: Boolean = false, // Pure light mode as per design system requirements
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = HighContrastTypography,
        shapes = RecepShapes,
        content = content
    )
}
