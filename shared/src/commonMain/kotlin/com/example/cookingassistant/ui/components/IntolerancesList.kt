package com.example.cookingassistant.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.example.cookingassistant.ui.screenmodel.Person
import com.example.cookingassistant.ui.theme.Dimens

// All available diets see: https://spoonacular.com/food-api/docs#Diets
val Diets = listOf(
    "Vegetarian",
    "Vegan",
    "Pescetarian",
    "Gluten Free",
)

// All available Intolerances see: https://spoonacular.com/food-api/docs#Intolerances
val Intolerances = listOf(
    "Dairy",
    "Egg",
    "Gluten",
    "Grain",
    "Peanut",
    "Seafood",
    "Sesame",
    "Shellfish",
    "Soy",
    "Sulfite",
    "Tree Nut",
    "Wheat",
)

@Composable
fun IntolerancesList(
    selectedPerson: Person,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.menuRowGap),
    ) {
        Text(
            text = "Intolerances · ${selectedPerson.name}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .border(
                    shape = MaterialTheme.shapes.small,
                    width = Dimens.borderWidth,
                    color = MaterialTheme.colorScheme.outline,
                ),
        ) {
            itemsIndexed(Diets + Intolerances) { index, intolerance ->
                val checked = intolerance in selectedPerson.intolerances
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.menuRowGap),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = Dimens.controlHeight)
                        .toggleable(
                            value = checked,
                            role = Role.Checkbox,
                            onValueChange = { onToggle(intolerance) },
                        )
                        .padding(start = Dimens.menuRowPadding),
                ) {
                    Text(
                        text = intolerance,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.padding(end = Dimens.menuRowGap),
                    )
                }
                if (index < (Diets + Intolerances).lastIndex) {
                    HorizontalDivider(
                        thickness = Dimens.hairlineWidth,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}
