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
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ImagePreviewScreenTest : BaseComposeTest() {

    private val imagePath: Path = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_preview_test.jpg"

    @AfterTest
    fun cleanUp() {
        FileSystem.SYSTEM.delete(imagePath, mustExist = false)
    }

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
    fun showsPhotoAndBothActionButtons() = runComposeUiTest {
        setTestContent {
            Navigator(ImagePreviewScreen(imagePath = imagePath.toString()))
        }

        onNodeWithTag("captured_photo").assertExists()
        onNodeWithTag("image_preview_retake").assertExists()
        onNodeWithTag("image_preview_confirm").assertExists()
    }

    @Test
    fun clickingRetakePopsNavigatorAndDeletesImage() = runComposeUiTest {
        FileSystem.SYSTEM.write(imagePath) { writeUtf8("placeholder") }
        lateinit var navigator: Navigator

        setTestContent {
            Navigator(listOf(HomeScreen(), ImagePreviewScreen(imagePath = imagePath.toString()))) { nav ->
                navigator = nav
                CurrentScreen()
            }
        }

        onNodeWithTag("image_preview_retake").performClick()

        assertTrue(navigator.lastItem is HomeScreen, "Clicking retake should pop back to the home screen")
        assertFalse(FileSystem.SYSTEM.exists(imagePath), "Clicking retake should delete the discarded image")
    }
}
