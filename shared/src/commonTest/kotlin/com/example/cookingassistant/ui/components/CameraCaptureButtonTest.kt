package com.example.cookingassistant.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.example.cookingassistant.ui.screens.BaseComposeTest
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class CameraCaptureButtonTest : BaseComposeTest() {

    @Test
    fun captureButtonFiresClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            CookingAssistantTheme {
                CameraCaptureButton(onClick = { clicks++ }, testTag = "capture_photo")
            }
        }

        onNodeWithTag("capture_photo").performClick()

        assertEquals(1, clicks)
    }
}
