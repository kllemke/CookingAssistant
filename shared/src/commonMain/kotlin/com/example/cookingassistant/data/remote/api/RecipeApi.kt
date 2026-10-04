package com.example.cookingassistant.data.remote.api

import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.data.remote.model.RecipeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RecipeApi(
    private val client: HttpClient
) {

    suspend fun getRecipes(ingredients: List<String>): List<Recipe> {
        return client
            .get("https://recipeapi.io/api/v1/recipes") {
                parameter("ingredients", ingredients.joinToString(","))
            }
            .body<RecipeResponse>()
            .data
    }
}