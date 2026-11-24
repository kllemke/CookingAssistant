package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class AnalyzedInstruction(
    val name: String = "",
    val steps: List<InstructionStep> = emptyList(),
)

@Serializable
data class InstructionStep(
    val number: Int,
    val step: String,
    val ingredients: List<StepItem> = emptyList(),
    val equipment: List<StepItem> = emptyList(),
    val length: StepLength? = null,
)

@Serializable
data class StepItem(
    val id: Int,
    val name: String,
    val localizedName: String? = null,
    val image: String? = null,
)

@Serializable
data class StepLength(
    val number: Int,
    val unit: String,
)