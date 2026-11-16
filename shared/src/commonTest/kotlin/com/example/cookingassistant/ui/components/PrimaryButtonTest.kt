package com.example.cookingassistant.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.example.cookingassistant.ui.screens.BaseComposeTest
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PrimaryButtonTest : BaseComposeTest() {

    @Test
    fun primaryButtonRendersLabelAndFiresClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            CookingAssistantTheme {
                PrimaryButton(text = "Speichern", onClick = { clicks++ }, testTag = "primary")
            }
        }

        onNodeWithText("Speichern").assertExists()
        onNodeWithTag("primary").assertHeightIsEqualTo(58.dp).performClick()

        assertEquals(1, clicks)
    }
}
