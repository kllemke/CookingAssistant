package com.example.cookingassistant.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cookingassistant.ui.theme.Dimens
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.ic_filter_alt
import cookingassistant.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

/**
 * Dropdown for filtering the recipe list by used ingredients.
 *
 * @param ingredients all ingredients that can be selected.
 * @param selectedIngredients the currently selected ingredients.
 * @param onToggle called with an ingredient when its row is selected / unselected.
 * @param onReset called when the reset button is clicked.
 * @param expanded whether the menu is open.
 * @param onExpandedChange called when the menu is opened or closed.
 */
@Composable
fun IngredientFilterDropdown(
    ingredients: List<String>,
    selectedIngredients: Set<String>,
    onToggle: (String) -> Unit,
    onReset: () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Dropdown(
        caption = "Zutaten",
        value = if (selectedIngredients.isNotEmpty()) "${selectedIngredients.size} ausgewählt" else "Alle",
        icon = Res.drawable.ic_filter_alt,
        expanded = expanded,
        highlighted = selectedIngredients.isNotEmpty(),
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        DropdownMenuRow {
            Text(
                "Rezepte anzeigen, die diese Zutaten verwenden",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (selectedIngredients.isNotEmpty()) {
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(Dimens.iconSizeExtraLarge),
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_refresh),
                        contentDescription = "Zurücksetzen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(Dimens.iconSizeSmall),
                    )
                }
            }
        }
        for (option in ingredients) {
            DropdownCheckRow(
                label = option,
                checked = option in selectedIngredients,
                onToggle = { onToggle(option) },
            )
        }
    }
}
