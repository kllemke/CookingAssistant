package com.example.cookingassistant.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.repository.RecipeRepository
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ImagePreviewScreenTest : BaseComposeTest() {

    /**
     * Fixture helper to set up DI container, theme, and composable content.
     */
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
    fun showsBothActionButtons() = runComposeUiTest {
        setTestContent {
            Navigator(ImagePreviewScreen(imagePath = "file:///tmp/unused.png"))
        }

        onNodeWithTag("image_preview_retake").assertExists()
        onNodeWithTag("image_preview_confirm").assertExists()
    }

    @Test
    fun clickingRetakePopsNavigator() = runComposeUiTest {
        var popped = false

        setTestContent {
            Navigator(HomeScreen()) { navigator ->
                if (navigator.lastItem is HomeScreen) {
                    navigator.push(ImagePreviewScreen(imagePath = "file:///tmp/unused.png"))
                } else {
                    popped = true
                }
                CurrentScreen()
            }
        }

        onNodeWithTag("image_preview_retake").performClick()

        assertTrue(popped, "Clicking retake should pop back to previous screen")
    }
}