package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResult(
    val offset: Int,
    val number: Int,
    val results: List<Recipe>,
    val totalResults: Int
)