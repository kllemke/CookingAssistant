package com.example.cookingassistant.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.screens.IngredientsScreen
import com.example.cookingassistant.ui.screens.RecipeScreen

@Composable
fun AppNavigationBar(navigator: Navigator) {
    val currentScreen = navigator.lastItem

    NavigationBar {
        NavigationBarItem(
            selected = currentScreen is HomeScreen,
            onClick = {
                if (currentScreen !is HomeScreen) {
                    navigator.replaceAll(HomeScreen())
                }
            },
            icon = { Icon(Icons.Default.Home, contentDescription = "Startseite") },
            label = { Text("Startseite") }
        )
        NavigationBarItem(
            selected = currentScreen is IngredientsScreen,
            onClick = {
                if (currentScreen !is IngredientsScreen) {
                    navigator.push(IngredientsScreen())
                }
            },
            icon = { Icon(Icons.Default.Checklist, contentDescription = "Zutaten") },
            label = { Text("Zutaten") }
        )
        NavigationBarItem(
            selected = currentScreen is RecipeScreen,
            onClick = {
                if (currentScreen !is RecipeScreen) {
                    navigator.push(RecipeScreen())
                }
            },
            icon = { Icon(Icons.Default.Preview, contentDescription = "Rezepte") },
            label = { Text("Rezepte") }
        )
    }
}