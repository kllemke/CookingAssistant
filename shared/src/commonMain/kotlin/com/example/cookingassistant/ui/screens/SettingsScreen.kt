package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow

class SettingsScreen : AppScreen {
    override val config = MutableStateFlow(ScreenConfig(title = "Settings"))

    @Composable
    override fun Content(){
        Box(modifier = Modifier.fillMaxSize())
    }
}
