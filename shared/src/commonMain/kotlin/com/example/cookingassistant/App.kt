package com.example.cookingassistant

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.cookingassistant.ui.screens.HomeScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        HomeScreen()
    }
}