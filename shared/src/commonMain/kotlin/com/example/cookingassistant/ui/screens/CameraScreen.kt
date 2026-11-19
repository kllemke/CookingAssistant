package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cookingassistant.ui.components.CameraView
import com.example.cookingassistant.ui.components.PrimaryButton
import com.example.cookingassistant.ui.theme.Dimens
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraScreen: AppScreen {
    override val config = MutableStateFlow(ScreenConfig(title = "Take photo of ingredients"))

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val cameraPermissionHandler = rememberCameraPermissionState()

        LaunchedEffect(cameraPermissionHandler.permissionState) {
            val granted = cameraPermissionHandler.permissionState == PermissionState.Granted
            config.update {
                it.copy(layout = if (granted) ScreenLayout.EdgeToEdge else ScreenLayout.Standard)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = if (cameraPermissionHandler.permissionState == PermissionState.Granted) 0.dp else Dimens.spacingSmall)
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
                    Text(
                        text = "Camera access required. Please enable it in the settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    PrimaryButton(
                        text = "Open settings",
                        onClick = { cameraPermissionHandler.openAppSettings() },
                        modifier = Modifier.align(Alignment.BottomCenter),
                        testTag = "open_settings"
                    )
                }
                PermissionState.Denied,
                PermissionState.NotGranted,
                PermissionState.NotDetermined -> {
                    // Permission not granted yet
                    PrimaryButton(
                        text = "Allow camera access",
                        onClick = { cameraPermissionHandler.requestPermission() },
                        modifier = Modifier.align(Alignment.Center),
                        testTag = "grant_camera_permission"
                    )
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
                    } catch (e: DeniedAlwaysException) {
                        permissionState = PermissionState.DeniedAlways
                    } catch (e: DeniedException) {
                        permissionState = PermissionState.Denied
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
