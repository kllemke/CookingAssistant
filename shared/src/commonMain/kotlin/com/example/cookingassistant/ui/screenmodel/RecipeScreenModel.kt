package com.example.cookingassistant.ui.screenmodel

import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.data.repository.RecipeRepository
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RecipeUiState {
    data object Idle : RecipeUiState
    data object Loading : RecipeUiState
    data class Success(val recipes: List<Recipe>) : RecipeUiState
    data class Error(val message: String) : RecipeUiState
}

class RecipeScreenModel(
    private val repository: RecipeRepository
) : ScreenModel {

    private val _recipeUiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val recipeUiState: StateFlow<RecipeUiState> = _recipeUiState.asStateFlow()

    fun loadRecipes(ingredients: List<String>) {
        if (ingredients.none { !it.isBlank() }) {
            _recipeUiState.value =
                RecipeUiState.Error("Bitte gib mindestens eine Zutat ein")
            return
        }

        screenModelScope.launch {
            _recipeUiState.value = RecipeUiState.Loading

            val result = repository.getRecipes(ingredients)

            result
                .onSuccess { recipes ->
                    _recipeUiState.value = RecipeUiState.Success(recipes)
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