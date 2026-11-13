package com.example.cookingassistant.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.cookingassistant.ui.screens.BaseComposeTest
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SecondaryButtonTest : BaseComposeTest() {

    @Test
    fun disabledButtonDoesNotFire() = runComposeUiTest {
        var clicks = 0
        setContent {
            CookingAssistantTheme {
                SecondaryButton(
                    text = "Erneut",
                    onClick = { clicks++ },
                    enabled = false,
                    testTag = "secondary"
                )
            }
        }

        onNodeWithTag("secondary").assertIsNotEnabled()

        assertEquals(0, clicks)
    }
}
