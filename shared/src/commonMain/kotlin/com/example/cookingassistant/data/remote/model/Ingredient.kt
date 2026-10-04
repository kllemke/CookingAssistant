package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val id: Int,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val optional: Boolean
)