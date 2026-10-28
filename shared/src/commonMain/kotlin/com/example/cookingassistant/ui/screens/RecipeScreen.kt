package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.components.RecipeListItem
import com.example.cookingassistant.ui.screenmodel.RecipeUiState

class RecipeScreen: Screen {
    @Composable
    override fun Content() {
        val appContainer = LocalAppContainer.current

        val screenModel = appContainer.recipeScreenModel
        val recipeUiState by screenModel.recipeUiState.collectAsState()
        val state = recipeUiState
        if (state is RecipeUiState.Success) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)

            ) {
                Text(
                    text = "Rezepte",
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .testTag("recipes_list"),
                ) {
                    for (recipe in state.recipes) {
                        RecipeListItem(recipe)
                    }
                }
            }
        }
    }
}