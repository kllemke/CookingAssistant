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
import cafe.adriel.voyager.core.screen.Screen
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

class CameraScreen: Screen {
    @Composable
    override fun Content() {
        //TODO: add Screen to take picture with the camera
        // Workflow:
        // 1. Scan the area (launch camera view) (bounding boxes will display there later!)
        // 2. Take a picture
//         3. Navigate to a Screen with Confirm (bounding boxes fixed), press on back for another try

        // MOCO's permission manager
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Check Camera Permission
            // Exhaustive cameraPermissionState Handling
            when (cameraPermissionState) {
                PermissionState.Granted -> {
                    CameraView()
                }
                PermissionState.DeniedAlways -> {
                    // Send to app settings, when "Don't ask again" was selected
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission is required. Please enable it in Settings.")
                        Button(onClick = { controller.openAppSettings() }) {
                            Text("Open Settings")
                        }
                    }
                }
                else -> {
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
                        Text("Grant Permission")
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
