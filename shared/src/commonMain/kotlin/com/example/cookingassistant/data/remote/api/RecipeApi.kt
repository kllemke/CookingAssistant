package com.example.cookingassistant.data.remote.api

import com.example.cookingassistant.data.remote.model.ApiResult
import com.example.cookingassistant.data.remote.model.Recipe
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RecipeApi(
    private val client: HttpClient
) {
    val BASE_URL = "https://api.spoonacular.com"

    // API reference: https://spoonacular.com/food-api/docs#Search-Recipes-Complex
    suspend fun getRecipes(ingredients: List<String>): List<Recipe> {
        return client
            .get("$BASE_URL/recipes/complexSearch") {
                parameter("instructionsRequired", true)
                parameter("addRecipeInformation", true)
                parameter("addRecipeInstructions", true)
                parameter("addRecipeNutrition", true)
                parameter("includeIngredients", ingredients.joinToString(","))
                parameter("fillIngredients", true)
                parameter("sort", "max-used-ingredients")
            }
            .body<ApiResult>().results
    }
}