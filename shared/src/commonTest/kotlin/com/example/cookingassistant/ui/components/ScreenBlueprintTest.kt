package com.example.cookingassistant.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.cookingassistant.ui.screens.BaseComposeTest
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import kotlin.test.Test

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
}
