package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    val currentPage: Int,
    val lastPage: Int,
    val path: String,
    val perPage: Int,
    val total: Int,
    val language: String
)