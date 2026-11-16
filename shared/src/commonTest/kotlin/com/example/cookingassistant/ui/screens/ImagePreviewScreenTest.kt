package com.example.cookingassistant.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.data.repository.RecipeRepository
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.theme.GreenColorScheme
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

    @Test
    fun showsPhotoAndBothActionButtons() = runComposeUiTest {
        val container = AppContainer(mock<RecipeRepository>())

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                MaterialTheme(colorScheme = GreenColorScheme) {
                    Navigator(ImagePreviewScreen(imagePath = imagePath.toString()))
                }
            }
        }

        onNodeWithTag("captured_photo").assertExists()
        onNodeWithTag("retake_button").assertExists()
        onNodeWithTag("confirm_button").assertExists()
    }

    @Test
    fun clickingRetakePopsNavigatorAndDeletesImage() = runComposeUiTest {
        val container = AppContainer(mock<RecipeRepository>())
        FileSystem.SYSTEM.write(imagePath) { writeUtf8("placeholder") }
        lateinit var navigator: Navigator

        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                MaterialTheme(colorScheme = GreenColorScheme) {
                    Navigator(listOf(HomeScreen(), ImagePreviewScreen(imagePath = imagePath.toString()))) { nav ->
                        navigator = nav
                        CurrentScreen()
                    }
                }
            }
        }

        onNodeWithTag("retake_button").performClick()

        assertTrue(navigator.lastItem is HomeScreen, "Clicking retake should pop back to the home screen")
        assertFalse(FileSystem.SYSTEM.exists(imagePath), "Clicking retake should delete the discarded image")
    }
}
