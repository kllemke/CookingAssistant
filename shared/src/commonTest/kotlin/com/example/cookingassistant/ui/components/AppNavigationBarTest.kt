package com.example.cookingassistant.ui.components

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.repository.RecipeRepository
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.screens.BaseComposeTest
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import dev.mokkery.mock
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppNavigationBarTest : BaseComposeTest() {

    /**
     * Fixture helper to set up navigation bar within CookingAssistantTheme and Voyager Navigator.
     */
    private fun ComposeUiTest.setNavBarContent(
        initialScreen: Screen = HomeScreen(),
        container: AppContainer = AppContainer(mock<RecipeRepository>())
    ) {
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                CookingAssistantTheme {
                    Navigator(initialScreen) { navigator ->
                        AppNavigationBar(navigator = navigator)
                        CurrentScreen()
                    }
                }
            }
        }
    }

    @Test
    fun testNavigateToIngredientsScreenFromNavBar() = runComposeUiTest {
        setNavBarContent()

        onNodeWithTag("bottom_bar").assertExists()
        onNodeWithTag("nav_ingredients").performClick()

        onNodeWithTag("enter_ingredients").assertExists()
    }

    @Test
    fun testIngredientsTabTwiceKeepsSingleScreen() = runComposeUiTest {
        setNavBarContent()

        onNodeWithTag("nav_ingredients").performClick()
        onNodeWithTag("nav_ingredients").performClick()

        onAllNodesWithTag("enter_ingredients").assertCountEquals(1)
    }
}