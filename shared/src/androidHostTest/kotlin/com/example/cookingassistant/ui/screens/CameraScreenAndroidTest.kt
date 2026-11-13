package com.example.cookingassistant.ui.screens

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.navigator.Navigator
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CameraScreenAndroidTest : BaseComposeTest() {

    @Test
    fun grantedPermissionHidesGrantButton() = runComposeUiTest {
        Shadows.shadowOf(RuntimeEnvironment.getApplication())
            .grantPermissions(Manifest.permission.CAMERA)

        setContent {
            Navigator(CameraScreen())
        }

        waitUntil {
            onAllNodes(hasTestTag("grant_camera_permission")).fetchSemanticsNodes().isEmpty()
        }
        onNodeWithTag("take_picture").assertExists()
    }
}
