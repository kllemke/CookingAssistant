package com.example.cookingassistant.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class Nutrition(
    val nutrients: List<Nutrient> = emptyList(),
    val properties: List<NutritionProperty> = emptyList(),
    val flavonoids: List<NutritionProperty> = emptyList(),
    val ingredients: List<IngredientNutrition> = emptyList(),
    val caloricBreakdown: CaloricBreakdown? = null,
    val weightPerServing: WeightPerServing? = null,
)

@Serializable
data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String,
    val percentOfDailyNeeds: Double? = null,
)

@Serializable
data class NutritionProperty(
    val name: String,
    val amount: Double,
    val unit: String,
)

@Serializable
data class IngredientNutrition(
    val id: Int,
    val name: String,
    val amount: Double,
    val unit: String,
    val nutrients: List<Nutrient> = emptyList(),
)

@Serializable
data class CaloricBreakdown(
    val percentProtein: Double,
    val percentFat: Double,
    val percentCarbs: Double,
)

@Serializable
data class WeightPerServing(
    val amount: Double,
    val unit: String,
)