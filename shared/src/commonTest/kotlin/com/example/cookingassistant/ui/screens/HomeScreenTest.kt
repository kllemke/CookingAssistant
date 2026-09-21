package com.example.cookingassistant.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.cookingassistant.App
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {

    @Test
    fun testNavigateToIngredientsScreenWorks() = runComposeUiTest {
        setContent {
            App()
        }

        onNodeWithTag("to_enter_manually").performClick()

        onNodeWithTag("enter_ingredients").assertExists()
    }

    @Test
    fun testNavigateToCameraScreenWorks() = runComposeUiTest {
        setContent {
            App()
        }

        onNodeWithTag("to_take_picture").performClick()

        onNodeWithTag("take_picture").assertExists()
    }
}