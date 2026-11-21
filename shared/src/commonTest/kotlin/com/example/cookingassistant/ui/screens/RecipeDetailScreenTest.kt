package com.example.cookingassistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.remote.model.AnalyzedInstruction
import com.example.cookingassistant.data.remote.model.Ingredient
import com.example.cookingassistant.data.remote.model.InstructionStep
import com.example.cookingassistant.data.remote.model.Nutrient
import com.example.cookingassistant.data.remote.model.Nutrition
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
        title = "Vegetable Stir-Fry",
        unusedIngredients = emptyList(),
        usedIngredientCount = 1,
        usedIngredients = listOf(
            Ingredient(
                aisle = "Produce",
                amount = 2.0,
                id = 1,
                image = "",
                meta = emptyList(),
                name = "Tomato",
                original = "2 tomatoes",
                originalName = "Tomato",
                unit = "piece",
                unitLong = "pieces",
                unitShort = "pc"
            )
        ),
        readyInMinutes = 20,
        servings = 2,
        nutrition = Nutrition(
            nutrients = listOf(
                Nutrient(name = "Calories", amount = 250.0, unit = "kcal"),
                Nutrient(name = "Protein", amount = 10.0, unit = "g"),
                Nutrient(name = "Carbohydrates", amount = 30.0, unit = "g"),
                // Fat is intentionally missing to test the "-" placeholder.
            )
        ),
        analyzedInstructions = listOf(
            AnalyzedInstruction(
                steps = listOf(
                    InstructionStep(number = 1, step = "Chop the vegetables."),
                    InstructionStep(number = 2, step = "Fry them in the pan."),
                )
            )
        ),
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

    private fun ComposeUiTest.showDetail(recipe: Recipe = sampleRecipe) {
        setTestContent {
            Navigator(RecipeDetailScreen(recipe))
        }
    }

    @Test
    fun screenConfigHasRecipeTitle() {
        val screen = RecipeDetailScreen(sampleRecipe)
        assertEquals("Vegetable Stir-Fry", screen.config.value.title)
    }

    @Test
    fun displaysScreenImageAndBackButton() = runComposeUiTest {
        showDetail()

        onNodeWithTag("recipe_detail_screen").assertIsDisplayed()
        onNodeWithTag("recipe_detail_image").assertIsDisplayed()
        onNodeWithTag("recipe_detail_back").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun displaysPreparationTimeAndServings() = runComposeUiTest {
        showDetail()

        onNodeWithText("20 min").assertIsDisplayed()
        onNodeWithText("2 servings").assertIsDisplayed()
    }

    @Test
    fun displaysNutritionValuesAndPlaceholderForMissingOnes() = runComposeUiTest {
        showDetail()

        onNodeWithText("250.0 kcal").performScrollTo().assertIsDisplayed() // Calories
        onNodeWithText("10.0 g").performScrollTo().assertIsDisplayed() // Protein
        onNodeWithText("30.0 g").performScrollTo().assertIsDisplayed() // Carbohydrates
        onNodeWithText("-").performScrollTo().assertIsDisplayed() // Fat
    }

    @Test
    fun displaysIngredients() = runComposeUiTest {
        showDetail()

        onNodeWithText("2 tomatoes").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun displaysPlaceholderWhenNoIngredients() = runComposeUiTest {
        showDetail(sampleRecipe.copy(usedIngredients = emptyList(), missedIngredients = emptyList()))

        onNodeWithText("No ingredients listed").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun displaysNumberedStepsFromAnalyzedInstructions() = runComposeUiTest {
        showDetail()

        onNodeWithText("1.").performScrollTo().assertIsDisplayed()
        onNodeWithText("Chop the vegetables.").performScrollTo().assertIsDisplayed()
        onNodeWithText("2.").performScrollTo().assertIsDisplayed()
        onNodeWithText("Fry them in the pan.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun showsNoStepsWhenAnalyzedInstructionsAreEmpty() = runComposeUiTest {
        showDetail(sampleRecipe.copy(analyzedInstructions = emptyList()))

        onAllNodesWithText("1.").assertCountEquals(0)
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
