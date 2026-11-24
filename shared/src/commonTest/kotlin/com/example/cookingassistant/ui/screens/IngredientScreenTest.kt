package com.example.cookingassistant.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.repository.RecipeRepository
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.screenmodel.Person
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class IngredientScreenTest : BaseComposeTest() {

    private fun ComposeUiTest.showIngredientsScreen(
        screen: IngredientsScreen,
        repository: RecipeRepository = mock(),
        persons: List<Person> = emptyList(),
    ) {
        val container = AppContainer(repository, FakePersonStorage(persons))
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                Navigator(screen)
            }
        }
    }

    private fun successfulRepository() = mock<RecipeRepository> {
        everySuspend { getRecipes(any(), any(), any()) } returns Result.success(emptyList())
    }

    @Test
    fun testIngredientInputWorks() = runComposeUiTest {
        var capturedText = ""
        showIngredientsScreen(
            IngredientsScreen(
                initialText = "Tomaten",
                onTextChange = { capturedText = it }
            )
        )

        // Check that the initial text is present
        onNodeWithTag("ingredients_input").assertTextContains("Tomaten")

        // Enter new text
        onNodeWithTag("ingredients_input").performTextInput(" und Gurken")

        // Check that the callback was invoked
        assertTrue(capturedText.contains("Gurken"))
    }

    @Test
    fun testSaveButtonUsesMockupCopy() = runComposeUiTest {
        showIngredientsScreen(IngredientsScreen(initialText = "Tomaten"))

        onNodeWithTag("save_button").assertTextContains("Save")
    }

    @Test
    fun testRecipeSearchWorks() = runComposeUiTest {
        val repository = successfulRepository()
        showIngredientsScreen(IngredientsScreen(initialText = "tomato\npotato"), repository)

        onNodeWithTag("save_button").performClick()

        verifySuspend {
            repository.getRecipes(listOf("tomato", "potato"), emptyList(), emptyList())
        }

        onNodeWithTag("recipes_list").assertExists()
    }

    @Test
    fun testRecipeSearchSendsIntolerancesAndDietsOfIncludedPersons() = runComposeUiTest {
        val repository = successfulRepository()
        showIngredientsScreen(
            IngredientsScreen(initialText = "tomato"),
            repository,
            persons = listOf(
                Person("Anna", intolerances = listOf("Vegan", "Dairy")),
                Person("Ben", included = false, intolerances = listOf("Gluten", "Vegetarian")),
                Person("Carl", intolerances = listOf("Dairy", "Peanut")),
            ),
        )

        onNodeWithTag("save_button").performClick()

        verifySuspend {
            repository.getRecipes(listOf("tomato"), listOf("Dairy", "Peanut"), listOf("Vegan"))
        }
    }
}
