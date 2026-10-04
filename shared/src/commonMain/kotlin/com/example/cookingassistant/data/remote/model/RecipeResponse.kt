package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class RecipeResponse(
    val data: List<Recipe>,
    val links: Links,
    val meta: Meta
)