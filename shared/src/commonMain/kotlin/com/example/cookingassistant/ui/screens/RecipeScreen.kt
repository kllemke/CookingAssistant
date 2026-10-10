package com.example.cookingassistant.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cafe.adriel.voyager.core.screen.Screen
import com.example.cookingassistant.di.LocalAppContainer

class RecipeScreen: Screen {
    @Composable
    override fun Content() {
        val appContainer = LocalAppContainer.current

        val screenModel = appContainer.recipeScreenModel
        val recipeUiState by screenModel.recipeUiState.collectAsState()

        // TODO: show list of recipes
        Text(
            text = recipeUiState.toString(),
            modifier = Modifier.testTag("recipe")
        )
    }
}