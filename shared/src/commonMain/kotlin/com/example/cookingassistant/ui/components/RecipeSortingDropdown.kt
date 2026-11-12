package com.example.cookingassistant.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cookingassistant.ui.screenmodel.RecipeSort
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.ic_swap_vert

/**
 * Dropdown for choosing the order of the recipe list.
 *
 * @param options the sort orders offered.
 * @param selected the current sort order.
 * @param onSelect called when sort order is selected.
 * @param expanded whether the menu is open.
 * @param onExpandedChange called when the menu is opened or closed.
 */
@Composable
fun RecipeSortingDropdown(
    options: List<RecipeSort>,
    selected: RecipeSort,
    onSelect: (RecipeSort) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Dropdown(
        caption = "Sortierung",
        value = selected.label,
        icon = Res.drawable.ic_swap_vert,
        expanded = expanded,
        highlighted = false,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
    ) {
        for (option in options) {
            OptionRow(
                label = option.label,
                selected = option == selected,
                onClick = {
                    onSelect(option)
                    onExpandedChange(false)
                },
            )
        }
    }
}
