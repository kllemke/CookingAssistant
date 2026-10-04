package com.example.cookingassistant.data.repository

import com.example.cookingassistant.data.remote.api.RecipeApi
import com.example.cookingassistant.data.remote.model.Recipe

interface RecipeRepository {
    suspend fun getRecipes(ingredients: List<String>): List<Recipe>
}

class RecipeRepositoryImpl(
    private val api: RecipeApi
) : RecipeRepository {

    override suspend fun getRecipes(ingredients: List<String>): List<Recipe> {
        return api.getRecipes(ingredients)
    }
}