package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: Int,
    val name: String,
    val description: String,
    val difficulty: String,
    val mealType: String,
    val cuisine: String,
    val dietaryTags: List<String>,
    val servings: Int,
    val prepTime: Int,
    val cookTime: Int,
    val caloriesPerServing: Int,
    val protein: Int,
    val instructions: List<String>,
    val ingredients: List<Ingredient>
)