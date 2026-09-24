package com.example.cookingassistant.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cafe.adriel.voyager.core.screen.Screen

class IngredientsScreen: Screen {
    @Composable
    override fun Content() {
        //TODO: add Screen to enter ingredients manually
        Text("Zutaten eingaben", modifier = Modifier.testTag("enter_ingredients"))
    }
}