package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Links(
    val first: String,
    val last: String,
    val prev: String?,
    val next: String?
)