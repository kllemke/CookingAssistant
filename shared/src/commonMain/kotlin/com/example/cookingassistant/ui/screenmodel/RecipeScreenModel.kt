package com.example.cookingassistant.ui.screenmodel

import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.data.repository.RecipeRepository
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RecipeSort(val label: String) {
    BEST_MATCH("Beste Übereinstimmung"),
    FEWEST_MISSING("Wenigste fehlende Zutaten"),
}

sealed interface RecipeUiState {
    data object Idle : RecipeUiState
    data object Loading : RecipeUiState
    data class Success(
        val allRecipes: List<Recipe>,
        val recipes: List<Recipe> = allRecipes,
        val selectedIngredients: Set<String> = emptySet(),
        val sort: RecipeSort = RecipeSort.BEST_MATCH,
    ) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

class RecipeScreenModel(
    private val repository: RecipeRepository
) : ScreenModel {

    private val _recipeUiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val recipeUiState: StateFlow<RecipeUiState> = _recipeUiState.asStateFlow()

    fun updateIngredientFilter(ingredient: String) {
        _recipeUiState.update { state ->
            if (state !is RecipeUiState.Success) return@update state
            val selected = state.selectedIngredients
            val newSelected = if (ingredient in selected) selected - ingredient else selected + ingredient
            state.copy(
                selectedIngredients = newSelected,
                recipes = state.allRecipes.filter { recipe ->
                    recipe.usedIngredients.map { it.name }.containsAll(newSelected)
                },
            )
        }
    }

    fun resetFilter() {
        _recipeUiState.update { state ->
            if (state !is RecipeUiState.Success) return@update state
            state.copy(selectedIngredients = emptySet(), recipes = state.allRecipes)
        }
    }

    fun setSort(sort: RecipeSort) {
        _recipeUiState.update { state ->
            if (state !is RecipeUiState.Success) return@update state
            val comparator = when (sort) {
                RecipeSort.BEST_MATCH -> compareByDescending<Recipe> { it.usedIngredientCount }
                RecipeSort.FEWEST_MISSING -> compareBy { it.missedIngredientCount }
            }
            state.copy(
                sort = sort,
                allRecipes = state.allRecipes.sortedWith(comparator),
                recipes = state.recipes.sortedWith(comparator),
            )
        }
    }

    fun loadRecipes(ingredients: List<String>) {
        if (ingredients.all { it.isBlank() }) {
            _recipeUiState.value =
                RecipeUiState.Error("Bitte gib mindestens eine Zutat ein")
            return
        }

        screenModelScope.launch {
            _recipeUiState.value = RecipeUiState.Loading

            val result = repository.getRecipes(ingredients)

            result
                .onSuccess { recipes ->
                    _recipeUiState.value = RecipeUiState.Success(
                        allRecipes = recipes.sortedByDescending { it.usedIngredientCount },
                    )
                }
                .onFailure { exception ->
                    _recipeUiState.value = RecipeUiState.Error(
                        exception.message ?: ""
                    )
                }
        }
    }

    fun resetRecipes() {
        _recipeUiState.value = RecipeUiState.Idle
    }
}