package com.example.cookingassistant.ui.screens

import androidx.compose.runtime.CompositionLocalProvider
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
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class IngredientScreenTest : BaseComposeTest() {

    @Test
    fun testIngredientInputWorks() = runComposeUiTest {
        var capturedText = ""

        val repository = mock<RecipeRepository>()
        val container = AppContainer(repository)

        setContent {
            CompositionLocalProvider(
                LocalAppContainer provides container
            ) {
                Navigator(
                    IngredientsScreen(
                        initialText = "Tomaten",
                        onTextChange = { capturedText = it }
                    )
                )
            }
        }

        // Check that the initial text is present
        onNodeWithTag("ingredients_input").assertTextContains("Tomaten")

        // Enter new text
        onNodeWithTag("ingredients_input").performTextInput(" und Gurken")

        // Check that the callback was invoked
        assertTrue(capturedText.contains("Gurken"))
    }

    @Test
    fun testSaveButtonUsesMockupCopy() = runComposeUiTest {
        val repository = mock<RecipeRepository>()
        val container = AppContainer(repository)

        setContent {
            CompositionLocalProvider(
                LocalAppContainer provides container
            ) {
                Navigator(
                    IngredientsScreen(initialText = "Tomaten")
                )
            }
        }

        onNodeWithTag("save_button").assertTextContains("Save")
    }

    @Test
    fun testRecipeSearchWorks() = runComposeUiTest {
        val repository = mock<RecipeRepository>()

        everySuspend {
            repository.getRecipes(listOf("tomato", "potato"), emptyList(), emptyList())
        } returns Result.success(emptyList())

        val container = AppContainer(repository)

        setContent {
            CompositionLocalProvider(
                LocalAppContainer provides container
            ) {
                Navigator(
                    IngredientsScreen(
                        initialText = "tomato\npotato"
                    )
                )
            }
        }

        onNodeWithTag("save_button").performClick()

        verifySuspend {
            repository.getRecipes(
                listOf("tomato", "potato"),
                emptyList(),
                emptyList()
            )
        }

        onNodeWithTag("recipes_list").assertExists()
    }
}
