package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cookingassistant.ui.components.CameraView
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch

class CameraScreen: AppScreen {
    override val showBottomBar: Boolean = false
    override val title: String = "Zutaten photographieren"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val cameraPermissionHandler = rememberCameraPermissionState()

        // Wrap the whole when-statement to center it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("take_picture"),
            contentAlignment = Alignment.Center
        ) {
            // Check Camera Permission
            // Exhaustive cameraPermissionState handling across Android & iOS
            when (cameraPermissionHandler.permissionState) {
                PermissionState.Granted -> {
                    CameraView(
                        onImageCaptured = { imagePath ->
                            navigator.push(
                                ImagePreviewScreen(imagePath = imagePath)
                            )
                        }
                    )
                }
                PermissionState.DeniedAlways -> {
                    // Send to app settings when permission was permanently denied
                    // (iOS "Don't Allow" / Android "Don't ask again")
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required. Please enable it in Settings.")
                        Button(onClick = { cameraPermissionHandler.openAppSettings() }) {
                            Text("Open Settings")
                        }
                    }
                }
                PermissionState.Denied,
                PermissionState.NotGranted,
                PermissionState.NotDetermined -> {
                    // Permission not granted yet
                    // (Android initial state is Denied/NotGranted; iOS is NotDetermined)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { cameraPermissionHandler.requestPermission() }) {
                            Text("Grant camera permission")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberCameraPermissionState(): CameraPermissionHandler {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }
    BindEffect(controller)

    val scope = rememberCoroutineScope()
    var permissionState by remember { mutableStateOf(PermissionState.NotDetermined) }

    LaunchedEffect(Unit) {
        permissionState = controller.getPermissionState(Permission.CAMERA)
    }

    return remember(controller, scope, permissionState) {
        CameraPermissionHandler(
            permissionState = permissionState,
            requestPermission = {
                scope.launch {
                    try {
                        controller.providePermission(Permission.CAMERA)
                        permissionState = PermissionState.Granted
                    } catch (e: DeniedException) {
                        permissionState = PermissionState.Denied
                    } catch (e: DeniedAlwaysException) {
                        permissionState = PermissionState.DeniedAlways
                    } catch (e: RequestCanceledException) {
                        permissionState = controller.getPermissionState(Permission.CAMERA)
                    } catch (e: Exception) {
                        permissionState = controller.getPermissionState(Permission.CAMERA)
                    }
                }
            },
            openAppSettings = {
                controller.openAppSettings()
            }
        )
    }
}

class CameraPermissionHandler(
    val permissionState: PermissionState,
    val requestPermission: () -> Unit,
    val openAppSettings: () -> Unit
)

@Preview
@Composable
fun PreviewCameraScreen(){
    CameraScreen().Content()
}
