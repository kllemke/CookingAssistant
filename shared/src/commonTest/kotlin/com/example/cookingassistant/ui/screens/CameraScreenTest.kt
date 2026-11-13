package com.example.cookingassistant.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.navigator.Navigator
import kotlin.test.Test
import kotlin.test.assertFalse

@OptIn(ExperimentalTestApi::class)
class CameraScreenTest : BaseComposeTest() {

    @Test
    fun showsGrantButtonWhenCameraPermissionIsNotGranted() = runComposeUiTest {
        setContent {
            Navigator(CameraScreen())
        }

        onNodeWithTag("take_picture").assertExists()
        onNodeWithTag("grant_camera_permission").assertExists()
    }

    @Test
    fun staysNonImmersiveWhilePermissionIsMissing() = runComposeUiTest {
        val screen = CameraScreen()

        setContent {
            Navigator(screen)
        }

        assertFalse(screen.immersive.value)
    }
}