package com.example.cookingassistant.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.cookingassistant.ui.screens.BaseComposeTest
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AppHeaderTest : BaseComposeTest() {

    @Test
    fun rendersTitle() = runComposeUiTest {
        setContent {
            CookingAssistantTheme {
                AppHeader(title = "Recipe Assistant")
            }
        }

        onNodeWithText("Recipe Assistant").assertExists()
    }

    @Test
    fun settingsButtonFiresClickWhenVisible() = runComposeUiTest {
        var clicks = 0
        setContent {
            CookingAssistantTheme {
                AppHeader(
                    title = "Cooking",
                    showSettingsButton = true,
                    onSettingsClick = { clicks++ },
                )
            }
        }

        onNodeWithContentDescription("Settings").performClick()
        assertEquals(1, clicks)
    }
}
