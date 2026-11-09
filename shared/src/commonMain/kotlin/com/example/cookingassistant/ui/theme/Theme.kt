package com.example.cookingassistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GreenColorScheme = lightColorScheme(
    primary = DarkGreen,
    onPrimary = Color.White,
    secondary = Green,
    onSecondary = Color.White,
    surface = LightGreen,
    onSurface = DarkerGreen,
    onSurfaceVariant = DarkerGreen.copy(alpha = 0.7f), // Unselected icon & text
    secondaryContainer = Green, // Active pill background
    onSecondaryContainer = Color.White, // Icon inside active pill
)

@Composable
fun CookingAssistantTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = GreenColorScheme,
        typography = AppTypography,
        content = content,
    )
}
