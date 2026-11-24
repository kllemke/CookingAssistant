package com.example.cookingassistant.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.example.cookingassistant.ml.AVAILABLE_DETECTION_MODELS
import com.example.cookingassistant.ml.DEFAULT_DETECTION_MODEL_ID
import com.example.cookingassistant.ui.components.CameraView
import com.example.cookingassistant.ui.components.DEFAULT_CONFIDENCE_THRESHOLD
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
import kotlin.math.roundToInt

class CameraScreen: AppScreen {
    override val config = MutableStateFlow(ScreenConfig(title = "Take photo of ingredients"))

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val cameraPermissionHandler = rememberCameraPermissionState()

        // Confidence threshold for the live detection overlay, adjustable via the FAB.
        var confidenceThreshold by rememberSaveable { mutableFloatStateOf(DEFAULT_CONFIDENCE_THRESHOLD) }
        // Selected detection model; session-persistent via saveable state.
        var selectedModelId by rememberSaveable { mutableStateOf(DEFAULT_DETECTION_MODEL_ID) }
        var showDetectionSettings by rememberSaveable { mutableStateOf(false) }

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
                        onImageCaptured = { photo ->
                            navigator.push(
                                ImagePreviewScreen(
                                    imagePath = photo.path,
                                    detections = photo.detections,
                                )
                            )
                        },
                        confidenceThreshold = confidenceThreshold,
                        selectedModelId = selectedModelId,
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

            if (cameraPermissionHandler.permissionState == PermissionState.Granted) {
                FloatingActionButton(
                    onClick = { showDetectionSettings = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = Dimens.spacingMedium, bottom = Dimens.spacingMedium)
                        .testTag("detection_settings_fab"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Detection settings",
                    )
                }
            }
        }

        if (showDetectionSettings) {
            DetectionSettingsSheet(
                threshold = confidenceThreshold,
                onThresholdChange = { confidenceThreshold = it },
                selectedModelId = selectedModelId,
                onModelChange = { selectedModelId = it },
                onDismiss = { showDetectionSettings = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetectionSettingsSheet(
    threshold: Float,
    onThresholdChange: (Float) -> Unit,
    selectedModelId: String,
    onModelChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("detection_settings_sheet"),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Detection settings",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Model",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AVAILABLE_DETECTION_MODELS.forEach { model ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("model_option_${model.id}")
                        .clickable { onModelChange(model.id) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = model.id == selectedModelId,
                        onClick = { onModelChange(model.id) },
                    )
                    Text(
                        text = model.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Confidence threshold",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatThreshold(threshold),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("confidence_value"),
                )
            }
            Slider(
                value = threshold,
                onValueChange = { raw ->
                    val stepped = (raw * 10f).roundToInt() / 10f
                    onThresholdChange(stepped.coerceIn(0.1f, 0.9f))
                },
                valueRange = 0.1f..0.9f,
                steps = 7,
                modifier = Modifier.testTag("confidence_slider"),
            )
            Text(
                text = "Only detected objects at or above this probability are shown.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PrimaryButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                testTag = "detection_settings_done",
            )
        }
    }
}

/** Formats 0.5f as "0.50" without relying on JVM-only String.format. */
private fun formatThreshold(value: Float): String {
    val hundredths = (value * 100f).roundToInt()
    val whole = hundredths / 100
    val fraction = (hundredths % 100).toString().padStart(2, '0')
    return "$whole.$fraction"
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
fun PreviewCameraScreen() {
    CameraScreen().Content()
}
