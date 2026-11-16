package com.example.cookingassistant.ui.screens

import kotlin.test.Test
import kotlin.test.assertFalse

class AppScreenTest {

    @Test
    fun screensOwnIndependentConfig() {
        val first = HomeScreen().config
        val second = SettingsScreen().config
        first.value = first.value.copy(layout = ScreenLayout.EdgeToEdge)
        assertFalse(second.value.isEdgeToEdge)
    }
}
