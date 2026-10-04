package com.example.cookingassistant.ui.screenmodel

import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.data.repository.RecipeRepository
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeScreenModel(
    private val repository: RecipeRepository
) : ScreenModel {

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes

    fun loadRecipes(ingredients: List<String>) {
        screenModelScope.launch {
            _recipes.value = repository.getRecipes(ingredients)
        }
    }
}