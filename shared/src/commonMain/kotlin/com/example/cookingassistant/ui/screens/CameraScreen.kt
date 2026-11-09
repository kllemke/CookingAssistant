package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
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
        // Workflow:
        // 1. Scan the area (launch camera view)
        // 2. Take a picture
        // 3. Navigate to another Screen with Confirm
        val navigator = LocalNavigator.currentOrThrow

        // moko-permissions manager
        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }
        BindEffect(controller)

        val cameraManagementScope = rememberCoroutineScope()

        // Initialize Camera Permission State
        var cameraPermissionState by remember { mutableStateOf(PermissionState.NotDetermined) }
        LaunchedEffect(Unit){
            cameraPermissionState = controller.getPermissionState(Permission.CAMERA)
        }

        // Wrap the whole when-statement to center it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("take_picture"),
            contentAlignment = Alignment.Center
        ) {
            // Check Camera Permission
            // Exhaustive cameraPermissionState handling across Android & iOS
            when (cameraPermissionState) {
                PermissionState.Granted -> {
                    CameraView(
                        onImageCaptured = {
                            imagePath -> navigator.push(
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
                        Button(onClick = { controller.openAppSettings() }) {
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = "Zutaten photographieren",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Button(
                            onClick = {
                                cameraManagementScope.launch {
                                    try {
                                        controller.providePermission(Permission.CAMERA)
                                        cameraPermissionState = PermissionState.Granted
                                    } catch (e: DeniedException) {
                                        cameraPermissionState = PermissionState.Denied
                                    } catch (e: DeniedAlwaysException) {
                                        cameraPermissionState = PermissionState.DeniedAlways
                                    } catch (e: RequestCanceledException) {
                                        cameraPermissionState =
                                            controller.getPermissionState(Permission.CAMERA)
                                    } catch (e: Exception) {
                                        cameraPermissionState =
                                            controller.getPermissionState(Permission.CAMERA)
                                    }
                                }
                            }
                        ) {
                            Text("Grant camera permission")
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewCameraScreen(){
    CameraScreen().Content()
}
