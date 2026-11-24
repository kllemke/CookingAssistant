package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.example.cookingassistant.data.remote.model.Nutrient
import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.ui.components.SecondaryButton
import com.example.cookingassistant.ui.theme.Dimens
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.ic_check
import cookingassistant.shared.generated.resources.ic_close
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.painterResource

data class RecipeDetailScreen(val recipe: Recipe) : AppScreen {
    override val config = MutableStateFlow(ScreenConfig(title = recipe.title))

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = Dimens.spacingLarge)
                .testTag("recipe_detail_screen"),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
        ) {
            // Recipe Image
            AsyncImage(
                model = recipe.image,
                contentDescription = "Image of ${recipe.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.recipeDetailImageHeight)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.secondary)
                    .border(Dimens.hairlineWidth, MaterialTheme.colorScheme.outlineVariant)
                    .testTag("recipe_detail_image")
            )

            // Meta Info: Time & Servings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMedium)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.spacingSmall),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Preparation time", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${recipe.readyInMinutes} min", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.spacingSmall),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Servings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${recipe.servings} servings", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Nutritional Values (Nährwertangaben)
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
            ) {
                Text(
                    text = "Nutrition facts (per serving)",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.secondary),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacingSmall),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        NutritionItem("Calories", recipe.nutrition.nutrients)
                        NutritionItem("Protein", recipe.nutrition.nutrients)
                        NutritionItem("Carbs", recipe.nutrition.nutrients, "Carbohydrates")
                        NutritionItem("Fat", recipe.nutrition.nutrients)
                    }
                }
            }

            // Ingredients (Zutaten)
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
            ) {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.secondary),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingExtraSmall)
                    ) {
                        val allIngredients = recipe.usedIngredients + recipe.missedIngredients
                        if (allIngredients.isEmpty()) {
                            Text("No ingredients listed", style = MaterialTheme.typography.bodyLarge)
                        } else {
                            val missedIds = recipe.missedIngredients.map { it.id }.toSet()
                            for (ingredient in allIngredients) {
                                val isMissing = ingredient.id in missedIds
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painterResource(if (isMissing) Res.drawable.ic_close else Res.drawable.ic_check),
                                        contentDescription = if (isMissing) "missing" else "available",
                                        tint = if (isMissing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(Dimens.iconSizeSmall)
                                    )
                                    Text(
                                        text = ingredient.original.ifBlank { "${ingredient.amount} ${ingredient.unit} ${ingredient.name}" },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Preparation Steps (Zubereitungsschritte)
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
            ) {
                Text(
                    text = "Preparation steps",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.secondary),
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.spacingSmall),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSmall)
                    ) {
                        val steps: List<String> = recipe.analyzedInstructions
                            .flatMap { it.steps }
                            .map { it.step }

                        steps.forEachIndexed { index, step ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = step.trim(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Back Button
            SecondaryButton(
                text = "Back to recipe list",
                onClick = { navigator.pop() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacingSmall)
                    .testTag("recipe_detail_back"),
            )
        }
    }
}

fun getAmountOfNutrition(nutrients: List<Nutrient>, name: String): String? {
    val nutrient = nutrients.firstOrNull { it.name.equals(name, ignoreCase = true) }
    if (nutrient != null) return "${ nutrient.amount } ${ nutrient.unit }"
    return null
}

@Composable
private fun NutritionItem(label: String, nutrients: List<Nutrient>, alternativeName: String? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = getAmountOfNutrition(nutrients, alternativeName ?: label) ?: "-", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}
