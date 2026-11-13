package com.example.cookingassistant.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.ui.screens.BaseComposeTest
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ScreenBlueprintTest : BaseComposeTest() {

    @Test
    fun rendersTitleAndContent() = runComposeUiTest {
        setContent {
            CookingAssistantTheme {
                ScreenBlueprint(title = "Zutaten hinzufügen", showBottomBar = false) {
                    Text("Inhalt", modifier = Modifier.testTag("blueprint_content"))
                }
            }
        }

        onNodeWithText("Zutaten hinzufügen").assertExists()
        onNodeWithTag("blueprint_content").assertExists()
    }

    @Test
    fun immersiveHidesChromeAndFillsTheScreen() = runComposeUiTest {
        setContent {
            CookingAssistantTheme {
                Navigator(HomeScreen()) { navigator ->
                    ScreenBlueprint(
                        title = "Zutaten hinzufügen",
                        showBottomBar = true,
                        navigator = navigator,
                        immersive = true,
                    ) {
                        Box(modifier = Modifier.fillMaxSize().testTag("immersive_content"))
                    }
                }
            }
        }

        onNodeWithText("Zutaten hinzufügen").assertDoesNotExist()
        onNodeWithTag("bottom_bar").assertDoesNotExist()
        // Immersive content must span the full width: no screenPadding gutter.
        // (Status-bar insets are zero under Robolectric, so only the gutter is provable here.)
        val root = onRoot().fetchSemanticsNode().size
        val content = onNodeWithTag("immersive_content").fetchSemanticsNode()
        assertEquals(0f, content.boundsInRoot.left)
        assertEquals(root.width, content.size.width)
    }
}
