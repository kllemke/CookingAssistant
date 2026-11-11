package com.example.cookingassistant.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.remote.model.Ingredient
import com.example.cookingassistant.data.remote.model.Recipe
import com.example.cookingassistant.data.repository.RecipeRepository
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.screenmodel.RecipeUiState
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecipeListScreenTest : BaseComposeTest() {
    private fun ComposeUiTest.showRecipeScreen(recipes: List<Recipe>) {
        val repository = mock<RecipeRepository>()
        everySuspend { repository.getRecipes(any()) } returns Result.success(recipes)

        val container = AppContainer(repository)
        val screenModel = container.recipeScreenModel
        screenModel.loadRecipes(listOf("tomato"))

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                Navigator(RecipeListScreen())
            }
        }

        waitUntil { screenModel.recipeUiState.value is RecipeUiState.Success }
    }

    private fun ComposeUiTest.menuEntry(text: String) =
        onNode(hasText(text) and hasAnyAncestor(isPopup()))

    private fun recipe(id: Int, title: String, used: List<String>, missedCount: Int) = Recipe(
        id = id,
        image = "",
        imageType = "jpg",
        likes = 0,
        missedIngredientCount = missedCount,
        missedIngredients = emptyList(),
        title = title,
        unusedIngredients = emptyList(),
        usedIngredientCount = used.size,
        usedIngredients = used.map(::ingredient),
    )

    private fun ingredient(name: String) = Ingredient(
        aisle = "",
        amount = 1.0,
        id = name.hashCode(),
        image = "",
        meta = emptyList(),
        name = name,
        original = name,
        originalName = name,
        unit = "",
        unitLong = "",
        unitShort = "",
    )


    private val pizza = recipe(id = 1, title = "Pizza", used = listOf("tomato", "cheese", "pepperoni"), missedCount = 3)
    private val salad = recipe(id = 2, title = "Salad", used = listOf("tomato", "cucumber"), missedCount = 1)

    @Test
    fun showsAllLoadedRecipes() = runComposeUiTest {
        showRecipeScreen(listOf(pizza, salad))

        onNodeWithText("Pizza").assertIsDisplayed()
        onNodeWithText("Salad").assertIsDisplayed()
        onNodeWithText("Zutaten").assert(hasText("Alle"))
    }

    @Test
    fun showsHintWhenNoRecipesFound() = runComposeUiTest {
        showRecipeScreen(emptyList())

        onNodeWithText("Keine Rezepte gefunden").assertIsDisplayed()
    }

    @Test
    fun showsHintWhenFilterMatchesNoRecipe() = runComposeUiTest {
        showRecipeScreen(listOf(pizza, salad))

        onNodeWithText("Zutaten").performClick()
        menuEntry("cheese").performClick()
        menuEntry("cucumber").performClick()

        onNodeWithText("Keine Rezepte für diesen Filter").assertExists()
        onNodeWithText("Pizza").assertDoesNotExist()
        onNodeWithText("Salad").assertDoesNotExist()
    }

    @Test
    fun ingredientFilterHidesRecipesWithoutThatIngredient() = runComposeUiTest {
        showRecipeScreen(listOf(pizza, salad))

        onNodeWithText("Zutaten").performClick()
        menuEntry("cheese").performClick()

        onNodeWithText("Pizza").assertExists()
        onNodeWithText("Salad").assertDoesNotExist()
        onNodeWithText("Zutaten").assert(hasText("1 ausgewählt"))
    }

    @Test
    fun resetShowsAllRecipesAgain() = runComposeUiTest {
        showRecipeScreen(listOf(pizza, salad))

        onNodeWithText("Zutaten").performClick()
        menuEntry("cheese").performClick()
        onNodeWithContentDescription("Zurücksetzen").performClick()

        onNodeWithText("Pizza").assertExists()
        onNodeWithText("Salad").assertExists()
        onNodeWithText("Zutaten").assert(hasText("Alle"))
    }

    @Test
    fun sortingChangesRecipeOrder() = runComposeUiTest {
        showRecipeScreen(listOf(salad, pizza))

        onNodeWithTag("recipes_list").onChildAt(0).assert(hasText("Pizza"))

        onNodeWithText("Sortierung").performClick()
        menuEntry("Wenigste fehlende Zutaten").performClick()

        onNodeWithTag("recipes_list").onChildAt(0).assert(hasText("Salad"))
        onNodeWithText("Sortierung").assert(hasText("Wenigste fehlende Zutaten"))
    }

    @Test
    fun filterKeepsChosenSortOrder() = runComposeUiTest {
        val pasta = recipe(id = 3, title = "Pasta", used = listOf("tomato"), missedCount = 1)
        showRecipeScreen(listOf(pizza, salad, pasta))

        onNodeWithText("Sortierung").performClick()
        menuEntry("Wenigste fehlende Zutaten").performClick()
        onNodeWithText("Zutaten").performClick()
        menuEntry("tomato").performClick()

        val list = onNodeWithTag("recipes_list")
        list.onChildAt(0).assert(hasText("Salad"))
        list.onChildAt(1).assert(hasText("Pasta"))
        list.onChildAt(2).assert(hasText("Pizza"))
    }
}
