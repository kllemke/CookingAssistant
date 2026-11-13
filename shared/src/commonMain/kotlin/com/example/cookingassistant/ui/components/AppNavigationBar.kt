package com.example.cookingassistant.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.screens.IngredientsScreen
import com.example.cookingassistant.ui.screens.RecipeListScreen
import com.example.cookingassistant.ui.theme.Dimens
import cookingassistant.shared.generated.resources.Res
import cookingassistant.shared.generated.resources.ic_home_filled
import cookingassistant.shared.generated.resources.ic_home_outlined
import cookingassistant.shared.generated.resources.ic_receipt_filled
import cookingassistant.shared.generated.resources.ic_receipt_outlined
import cookingassistant.shared.generated.resources.ic_restaurant_filled
import cookingassistant.shared.generated.resources.ic_restaurant_outlined
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppNavigationBar(
    navigator: Navigator,
    modifier: Modifier = Modifier
) {
    val currentScreen = navigator.lastItem

    NavigationBar(
        containerColor = Color.Transparent,
        windowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .padding(top = Dimens.spacingMedium)
            .height(Dimens.navBarHeight)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = Dimens.hairlineWidth,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.extraLarge,
            )
            .testTag("bottom_bar"),
    ) {
        NavigationBarItem(
            selected = currentScreen is HomeScreen,
            onClick = {
                if (currentScreen !is HomeScreen) {
                    navigator.replaceAll(HomeScreen())
                }
            },
            icon = {
                Icon(painter = painterResource(
                        if (currentScreen is HomeScreen)
                            Res.drawable.ic_home_filled
                        else
                            Res.drawable.ic_home_outlined
                    ),
                    contentDescription = "Startseite"
                )
            },
            label = { Text("Startseite") },
            colors = navigationItemColors(),
            modifier = Modifier.testTag("nav_home"),
        )
        NavigationBarItem(
            selected = currentScreen is IngredientsScreen,
            onClick = {
                if (currentScreen !is IngredientsScreen) {
                    navigator.push(IngredientsScreen())
                }
            },
            icon = {
                Icon(
                    painter = painterResource(
                        if (currentScreen is IngredientsScreen)
                            Res.drawable.ic_restaurant_filled
                        else
                            Res.drawable.ic_restaurant_outlined
                    ),
                    contentDescription = "Zutaten",
                )
            },
            label = { Text("Zutaten") },
            colors = navigationItemColors(),
            modifier = Modifier.testTag("nav_ingredients"),
        )
        NavigationBarItem(
            selected = currentScreen is RecipeListScreen,
            onClick = {
                if (currentScreen !is RecipeListScreen) {
                    navigator.push(RecipeListScreen())
                }
            },
            icon = {
                Icon(
                    painter = painterResource(
                        if (currentScreen is RecipeListScreen)
                            Res.drawable.ic_receipt_filled
                        else
                            Res.drawable.ic_receipt_outlined
                    ),
                    contentDescription = "Rezepte")
                   },
            label = { Text("Rezepte") },
            colors = navigationItemColors(),
            modifier = Modifier.testTag("nav_recipes"),
        )
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedTextColor = MaterialTheme.colorScheme.onSurface,
    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)
