package com.example.cookingassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val GreenColorScheme = lightColorScheme(
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

@Composable
fun CookingAssistantTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GreenColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
