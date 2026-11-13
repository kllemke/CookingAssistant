package com.example.cookingassistant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.example.cookingassistant.di.AppContainer
import com.example.cookingassistant.di.LocalAppContainer
import com.example.cookingassistant.ui.components.ScreenBlueprint
import com.example.cookingassistant.ui.screens.AppScreen
import com.example.cookingassistant.ui.screens.CameraScreen
import com.example.cookingassistant.ui.screens.HomeScreen
import com.example.cookingassistant.ui.screens.LocalCameraPermissionHandler
import com.example.cookingassistant.ui.screens.rememberCameraPermissionState
import com.example.cookingassistant.ui.theme.CookingAssistantTheme
import dev.icerock.moko.permissions.PermissionState

@Composable
@Preview
fun App() {
    val appContainer = remember {
        AppContainer()
    }

    CompositionLocalProvider(
        LocalAppContainer provides appContainer,
    ) {
        CookingAssistantTheme {
            Navigator(HomeScreen()) { navigator ->
                val currentScreen = navigator.lastItem as? AppScreen
                val cameraPermissionHandler = if (currentScreen is CameraScreen) {
                    rememberCameraPermissionState()
                } else null

                val isCameraGranted = currentScreen is CameraScreen &&
                        cameraPermissionHandler?.permissionState == PermissionState.Granted

                val title = if (isCameraGranted) null else currentScreen?.title
                val showBottomBar = if (isCameraGranted) false else (currentScreen?.showBottomBar ?: true)

                CompositionLocalProvider(
                    LocalCameraPermissionHandler provides cameraPermissionHandler
                ) {
                    ScreenBlueprint(
                        title = title,
                        showBottomBar = showBottomBar,
                        navigator = navigator,
                    ) {
                        CurrentScreen()
                    }
                }
            }
        }
    }
}