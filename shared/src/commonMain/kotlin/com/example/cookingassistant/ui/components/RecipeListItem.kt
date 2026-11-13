package com.example.cookingassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.ui.theme.Dimens
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.ic_check
import cookingassistant.shared.generated.resources.ic_chevron_right
import org.jetbrains.compose.resources.painterResource

private const val MAX_INGREDIENTS = 2

@Composable
fun RecipeListItem(recipe: Recipe) {
    val remaining = recipe.missedIngredientCount + recipe.usedIngredientCount - MAX_INGREDIENTS

    Card (
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.secondary),
        onClick = {
            // TODO: navigate to recipe details screen
            // navigator.push(RecipeDetailScreen(recipe))
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSmall),
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingSmall)
        ) {
            AsyncImage(
                model = recipe.image,
                contentDescription = "Bild von ${recipe.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(Dimens.recipeImageSize)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.colorScheme.secondary)
                    .border(Dimens.hairlineWidth, MaterialTheme.colorScheme.outlineVariant)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.weight(1f)
            )  {
                Text(
                    recipe.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow= TextOverflow.Ellipsis,
                    maxLines = 1
                )
                for (ingredient in recipe.usedIngredients.take(MAX_INGREDIENTS)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            painterResource(Res.drawable.ic_check),
                            contentDescription = "vorhanden",
                            modifier = Modifier.size(Dimens.iconSizeSmall)
                        )
                        Text(
                            ingredient.name,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (remaining > 0) {
                    Text(
                        "und $remaining weitere",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 22.dp)
                    )
                }
            }
            Icon(
                painterResource(Res.drawable.ic_chevron_right),
                contentDescription = "rezept öffnen",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(Dimens.iconSizeExtraLarge).align(Alignment.CenterVertically)
            )
        }
    }
}
