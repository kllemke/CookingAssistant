package com.example.cookingassistant.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.cookingassistant.di.LocalAppContainer

class RecipeScreen: AppScreen {
    override val title: String = "Rezepte"
    override val showBottomBar: Boolean = true

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