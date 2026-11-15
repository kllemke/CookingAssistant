package com.example.cookingassistant.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val GreenLightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = LightGreen,
    secondary = Green,
    onSecondary = LightGreen,
    secondaryContainer = Green, // Active nav pill
    onSecondaryContainer = LightGreen,
    background = White,
    onBackground = Ink,
    surface = White,
    onSurface = Ink,
    surfaceVariant = LightGreen,
    onSurfaceVariant = InkMuted, // Unselected icon & text
    surfaceContainer = LightGreen, // Cards, nav bar, text field
    outline = Green,
    outlineVariant = Grey,
)

val GreenDarkColorScheme = darkColorScheme(
    primary = GreenDarkPrimary,
    onPrimary = InkDarkBackground,
    secondary = GreenDarkPrimary,
    onSecondary = InkDarkBackground,
    secondaryContainer = GreenDarkPrimary,
    onSecondaryContainer = InkDarkBackground,
    background = InkDarkBackground,
    onBackground = InkDarkText,
    surface = InkDarkBackground,
    onSurface = InkDarkText,
    surfaceVariant = InkDarkSurfaceContainer,
    onSurfaceVariant = InkDarkMutedText,
    surfaceContainer = InkDarkSurfaceContainer,
    outline = GreenDarkPrimary,
    outlineVariant = GreyDarkOutline,
)

@Composable
fun CookingAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
