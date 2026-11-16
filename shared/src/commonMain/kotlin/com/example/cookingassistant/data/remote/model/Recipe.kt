package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

// API reference: https://spoonacular.com/food-api/docs
@Serializable
data class Recipe(
    val id: Int,
    val image: String,
    val imageType: String,
    val likes: Int,
    val missedIngredientCount: Int,
    val missedIngredients: List<Ingredient>,
    val title: String,
    val unusedIngredients: List<Ingredient>,
    val usedIngredientCount: Int,
    val usedIngredients: List<Ingredient>,
    val summary: String? = null,
    val instructions: String? = null,
    val readyInMinutes: Int? = 30,
    val servings: Int? = 4,
    val calories: Int? = 350,
    val protein: String? = "15g",
    val carbs: String? = "45g",
    val fat: String? = "12g"
)
