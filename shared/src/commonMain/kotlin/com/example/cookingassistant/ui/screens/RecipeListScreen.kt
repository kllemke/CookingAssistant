package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.components.IngredientFilterDropdown
import com.example.cookingassistant.ui.components.RecipeListItem
import com.example.cookingassistant.ui.components.RecipeSortingDropdown
import com.example.cookingassistant.ui.screenmodel.RecipeSort
import com.example.cookingassistant.ui.screenmodel.RecipeUiState

private enum class RecipeListMenu { INGREDIENTS, SORT }

class RecipeListScreen: Screen {
    @Composable
    override fun Content() {
        val appContainer = LocalAppContainer.current

        val screenModel = appContainer.recipeScreenModel
        val recipeUiState by screenModel.recipeUiState.collectAsState()
        val state = recipeUiState

        var openMenu by rememberSaveable { mutableStateOf<RecipeListMenu?>(null) }

        if (state is RecipeUiState.Success) {
            val visibleRecipes = state.recipes
            val ingredients: List<String> = remember(state.allRecipes) {
                state.allRecipes
                    .flatMap { recipe -> recipe.usedIngredients.map { it.name } }
                    .distinct()
                    .sorted()
            }

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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    IngredientFilterDropdown(
                        ingredients = ingredients,
                        selectedIngredients = state.selectedIngredients,
                        onToggle = screenModel::updateIngredientFilter,
                        onReset = screenModel::resetFilter,
                        expanded = openMenu == RecipeListMenu.INGREDIENTS,
                        onExpandedChange = { openMenu = if (it) RecipeListMenu.INGREDIENTS else null },
                        modifier = Modifier.weight(1f),
                    )
                    RecipeSortingDropdown(
                        options = RecipeSort.entries,
                        selected = state.sort,
                        onSelect = screenModel::setSort,
                        expanded = openMenu == RecipeListMenu.SORT,
                        onExpandedChange = { openMenu = if (it) RecipeListMenu.SORT else null },
                        modifier = Modifier.weight(1f),
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .testTag("recipes_list"),
                ) {
                    if (state.allRecipes.isEmpty()) {
                        Text("Keine Rezepte gefunden", color = Color.Gray)
                    } else if (visibleRecipes.isEmpty()) {
                        Text("Keine Rezepte für diesen Filter", color = Color.Gray)
                    }
                    for (recipe in visibleRecipes) {
                        RecipeListItem(recipe)
                    }
                }
            }
        }
    }
}