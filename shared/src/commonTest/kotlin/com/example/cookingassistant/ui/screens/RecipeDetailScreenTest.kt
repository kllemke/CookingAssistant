package com.example.cookingassistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.remote.model.Ingredient
import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.data.repository.RecipeRepository
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RecipeDetailScreenTest : BaseComposeTest() {

    private val sampleRecipe = Recipe(
        id = 101,
        image = "",
        imageType = "jpg",
        likes = 10,
        missedIngredientCount = 0,
        missedIngredients = emptyList(),
        title = "Gemüse-Pfanne",
        unusedIngredients = emptyList(),
        usedIngredientCount = 2,
        usedIngredients = listOf(
            Ingredient(
                aisle = "Produce",
                amount = 2.0,
                id = 1,
                image = "",
                meta = emptyList(),
                name = "Tomate",
                original = "2 Tomaten",
                originalName = "Tomate",
                unit = "Stück",
                unitLong = "Stücke",
                unitShort = "Stk"
            )
        ),
        summary = "Eine leckere und gesunde Gemüsepfanne.",
        instructions = "1. Gemüse schneiden.\n2. In der Pfanne anbraten.",
        readyInMinutes = 20,
        servings = 2,
        calories = 250,
        protein = "10g",
        carbs = "30g",
        fat = "8g"
    )

    private fun ComposeUiTest.setTestContent(
        container: AppContainer = AppContainer(mock<RecipeRepository>()),
        content: @Composable () -> Unit
    ) {
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                CookingAssistantTheme {
                    content()
                }
            }
        }
    }

    @Test
    fun screenConfigHasRecipeTitle() {
        val screen = RecipeDetailScreen(sampleRecipe)
        assertEquals("Gemüse-Pfanne", screen.config.value.title)
    }

    @Test
    fun displaysAllRecipeDetails() = runComposeUiTest {
        setTestContent {
            Navigator(RecipeDetailScreen(sampleRecipe))
        }

        onNodeWithTag("recipe_detail_screen").assertIsDisplayed()
        onNodeWithTag("recipe_detail_title").assertIsDisplayed()
        onNodeWithTag("recipe_detail_image").assertIsDisplayed()
        onNodeWithTag("recipe_detail_back").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun clickingBackPopsNavigator() = runComposeUiTest {
        lateinit var navigator: Navigator

        setTestContent {
            Navigator(listOf(RecipeListScreen(), RecipeDetailScreen(sampleRecipe))) { nav ->
                navigator = nav
                CurrentScreen()
            }
        }

        onNodeWithTag("recipe_detail_back").performScrollTo().performClick()

        assertTrue(navigator.lastItem is RecipeListScreen, "Clicking back should pop back to recipe list screen")
    }
}
