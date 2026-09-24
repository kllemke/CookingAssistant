package com.example.cookingassistant.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import cafe.adriel.voyager.navigator.Navigator
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class IngredientScreenTest : BaseComposeTest() {

    @Test
    fun testIngredientInputWorks() = runComposeUiTest {
        var capturedText = ""
        
        setContent {
            Navigator(
                IngredientsScreen(
                    initialText = "Tomaten",
                    onTextChange = { capturedText = it }
                )
            )
        }

        // Überprüfen, ob der initiale Text vorhanden ist
        onNodeWithTag("zutaten_input").assertTextContains("Tomaten")

        // Neuen Text eingeben
        onNodeWithTag("zutaten_input").performTextInput(" und Gurken")

        // Überprüfen, ob der Callback aufgerufen wurde
        assertTrue(capturedText.contains("Gurken"))
    }
}
