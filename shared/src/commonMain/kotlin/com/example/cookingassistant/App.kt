package com.example.cookingassistant

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.theme.GreenColorScheme

@Composable
@Preview
fun App() {
    MaterialTheme (
        colorScheme = GreenColorScheme
    ) {
        Navigator(HomeScreen()) { navigator ->
            CurrentScreen()
        }
    }
}