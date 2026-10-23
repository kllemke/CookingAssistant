package com.example.cookingassistant.data.remote.api

import com.example.cookingassistant.data.remote.model.Recipe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RecipeApi(
    private val client: HttpClient
) {
    val BASE_URL = "https://api.spoonacular.com"

    // API reference: https://spoonacular.com/food-api/docs#Search-Recipes-by-Ingredients
    suspend fun getRecipes(ingredients: List<String>): List<Recipe> {
        return client
            .get("$BASE_URL/recipes/findByIngredients") {
                parameter("ingredients", ingredients.joinToString(","))
            }
            .body<List<Recipe>>()
    }
}