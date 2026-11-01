package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val aisle: String,
    val amount: Double,
    val id: Int,
    val image: String,
    val meta: List<String>,
    val name: String,
    val original: String,
    val originalName: String,
    val unit: String,
    val unitLong: String,
    val unitShort: String,
    val extendedName: String? = null
)