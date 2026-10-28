package com.example.cookingassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.cookingassistant.data.remote.model.Recipe

@Composable
fun RecipeListItem(recipe: Recipe) {
    Card (
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(4.dp, MaterialTheme.colorScheme.secondary),
        onClick = {
            // TODO: navigate to recipe details screen
            // navigator.push(RecipeDetailScreen(recipe))
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            AsyncImage(
                model = recipe.image,
                contentDescription = "image of recipe"
            )
            Column {
                Text(
                    recipe.title,
                    fontSize = 12.sp,
                    overflow= TextOverflow.Ellipsis,
                    maxLines = 1
                )
                for (ingredient in recipe.usedIngredients) {
                    Text(
                        "- "+ ingredient.name,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}