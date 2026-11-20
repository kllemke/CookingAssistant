package com.example.cookingassistant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.local.InMemoryPersonStorage
import com.example.cookingassistant.data.local.PersonStorage
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.components.ScreenBlueprint
import com.example.cookingassistant.ui.screens.AppScreen
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.screens.SettingsScreen
import com.example.cookingassistant.ui.theme.CookingAssistantTheme

@Composable
@Preview
fun App(
    start: AppScreen = HomeScreen(),
    personStorage: PersonStorage = InMemoryPersonStorage(),
) {
    val appContainer = remember {
        AppContainer(personStorage = personStorage)
    }

    CompositionLocalProvider(
        LocalAppContainer provides appContainer,
    ) {
        CookingAssistantTheme {
            Navigator(start) { navigator ->
                val currentScreen = navigator.lastItem as? AppScreen
                    ?: error("Every screen must implement AppScreen")
                val config by currentScreen.config.collectAsState()

                ScreenBlueprint(
                    config = config,
                    onSettingsClick = { navigator.push(SettingsScreen()) },
                    navigator = navigator,
                ) {
                    CurrentScreen()
                }
            }
        }
    }
}
