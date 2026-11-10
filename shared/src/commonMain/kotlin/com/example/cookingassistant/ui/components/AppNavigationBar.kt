package com.example.cookingassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.screens.IngredientsScreen
import com.example.cookingassistant.ui.screens.RecipeScreen
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
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
//            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(80.dp)
            )
            .padding(top = 10.dp)
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
            label = { Text("Startseite") }
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
            label = { Text("Zutaten") }
        )
        NavigationBarItem(
            selected = currentScreen is RecipeScreen,
            onClick = {
                if (currentScreen !is RecipeScreen) {
                    navigator.push(RecipeScreen())
                }
            },
            icon = {
                Icon(
                    painter = painterResource(
                        if (currentScreen is RecipeScreen)
                            Res.drawable.ic_receipt_filled
                        else
                            Res.drawable.ic_receipt_outlined
                    ),
                    contentDescription = "Rezepte")
                   },
            label = { Text("Rezepte") }
        )
    }
}