package com.example.cookingassistant.ui.components

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.takePicture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.cookingassistant.ml.CapturedPhoto
import com.example.cookingassistant.ml.DetectionEngine
import com.example.cookingassistant.ml.DetectionFrame
import com.example.cookingassistant.ml.DetectionModel
import com.example.cookingassistant.ml.FrameDetector
import com.example.cookingassistant.ml.LiteRTYoloDetector
import com.example.cookingassistant.ml.MediaPipeObjectDetector
import com.example.cookingassistant.ml.detectionModelById
import com.example.cookingassistant.ml.normalizedDetections
import com.example.cookingassistant.ui.theme.CameraOverlay
import com.example.cookingassistant.ui.theme.Dimens
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger {}

@Composable
actual fun CameraView(
    onImageCaptured: (CapturedPhoto) -> Unit,
    modifier: Modifier,
    confidenceThreshold: Float,
    selectedModelId: String,
) {
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Camera use cases
    val previewView = remember {
        PreviewView(context).apply {
            // TextureView-backed preview so the Compose detection overlay can
            // be drawn above it.
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // The selected model; unknown ids fall back to the default (registry).
    val model = detectionModelById(selectedModelId)

    val detectionFlow = remember { MutableStateFlow<DetectionFrame?>(null) }
    // Error state is per model: a switch starts with a clean slate, and a
    // late error from the outgoing detector cannot stick to the incoming one.
    val detectorErrorFlow = remember(model.id) { MutableStateFlow<String?>(null) }
    var isModelLoading by remember { mutableStateOf(false) }

    // Detector and its single analyzer thread live and die together per model:
    // a switch disposes the outgoing pair (closing the detector on its own
    // thread after unbinding) and the incoming model gets a fresh pair.
    val analyzerExecutor = remember(model.id) { Executors.newSingleThreadExecutor() }
    val detector: FrameDetector = remember(model.id) {
        createDetector(context, model).apply {
            onDetection = { frame -> detectionFlow.value = frame }
            onError = { message -> detectorErrorFlow.value = message }
        }
    }

    // Track if camera initialization failed
    var hasCameraError by remember { mutableStateOf(false) }

    // Capture state: freeze the preview on shutter so the user sees the exact
    // moment that is being saved, then hand photo + detections to the caller.
    var isCapturing by remember { mutableStateOf(false) }
    var frozenFrame by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(model.id) {
        val cameraProvider = cameraProviderFuture.await()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        // Load the model off the main thread; the preview stays usable while
        // it loads, and a status chip flags the switch.
        isModelLoading = true
        val detectorReady = try {
            withContext(analyzerExecutor.asCoroutineDispatcher()) {
                detector.setup()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            // Throwable, not Exception: on the JVM host (Robolectric) the
            // native LiteRT runtime is absent and setup fails with an Error;
            // the preview must survive that exactly like a runtime failure.
            logger.warn(e) { "Object detector setup failed: ${e.message}" }
            false
        }
        isModelLoading = false
        if (!detectorReady) {
            logger.warn { "Object detector unavailable; camera preview continues without overlay." }
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also { analysis ->
                analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                    detector.detect(imageProxy)
                }
            }

        try {
            // Rebinding also swaps the analyzer to the newly selected model.
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifeCycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis,
            )
            hasCameraError = false
        } catch (e: Exception) {
            // No need for capturing specific exceptions here
            // catching as such prevents the app from crashing
            logger.error(e) { "Failed to bind CameraX: ${e.message}" }
            hasCameraError = true
        }
    }

    // Clean up this model's detector and analyzer thread when the model
    // changes or the screen leaves: unbind first, then clear on the same
    // single thread so in-flight frames finish before the native model closes.
    DisposableEffect(model.id) {
        onDispose {
            try {
                cameraProviderFuture.get()?.unbindAll()
            } catch (e: Exception) {
                logger.warn(e) { "Failed to unbind CameraX on dispose: ${e.message}" }
            }
            analyzerExecutor.execute { detector.clear() }
            analyzerExecutor.shutdown()
        }
    }

    val detectionFrame by detectionFlow.collectAsState()
    val detectorError by detectorErrorFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Alternative UI for when camera hardware fails
        if (hasCameraError) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "Camera error: Camera could not be initialized.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                )

                // Frozen frame: drawn above the live preview so the shutter
                // freezes the visible camera state until the photo is saved.
                frozenFrame?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("frozen_camera_frame"),
                    )
                }

                detectionFrame?.let { frame ->
                    DetectionOverlay(
                        frame = frame,
                        confidenceThreshold = confidenceThreshold,
                        modifier = Modifier.fillMaxSize(),
                    )
                    DetectionStatusChip(
                        text = "${frame.visibleObjects(confidenceThreshold).size} objects · ${frame.inferenceTimeMs} ms",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(Dimens.spacingSmall)
                            .testTag("detection_stats"),
                    )
                }

                detectorError?.let { message ->
                    DetectionStatusChip(
                        text = message,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(Dimens.spacingSmall)
                            .testTag("detection_error"),
                    )
                }

                if (isModelLoading) {
                    DetectionStatusChip(
                        text = "Loading model…",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(Dimens.spacingSmall)
                            .testTag("model_loading"),
                    )
                }

                // Trigger button
                CameraCaptureButton(
                    onClick = {
                        if (isCapturing) return@CameraCaptureButton

                        // Freeze what the user sees right now and snapshot the
                        // detections so the preview can show photo + boxes.
                        isCapturing = true
                        frozenFrame = detector.lastPreviewBitmap?.asImageBitmap()
                        val detections = detectionFrame
                            ?.normalizedDetections(confidenceThreshold)
                            .orEmpty()

                        lifeCycleOwner.lifecycleScope.launch {
                            val photoUri = takePhoto(context, imageCapture)
                            if (photoUri != null) {
                                onImageCaptured(
                                    CapturedPhoto(
                                        path = photoUri.toString(),
                                        detections = detections,
                                    )
                                )
                            } else {
                                // Camera still works; let the user try again.
                                isCapturing = false
                                frozenFrame = null
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = Dimens.spacingMedium),
                    testTag = "capture_photo",
                )
            }
        }
    }
}

/**
 * Maps a registry entry to the engine that runs it — the single place where
 * the LiteRT/MediaPipe implementation detail is chosen.
 */
private fun createDetector(context: Context, model: DetectionModel): FrameDetector =
    when (model.engine) {
        DetectionEngine.LITERT -> LiteRTYoloDetector(context, modelAsset = model.assetFile)
        DetectionEngine.MEDIAPIPE -> MediaPipeObjectDetector(context, modelAsset = model.assetFile)
    }

@Composable
private fun DetectionStatusChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = CameraOverlay,
        )
    }
}

// Taken from Looksy
private suspend fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
): Uri? {
    // Save photos taken in the local cache -> it's safer, no additional permissions needed
    val photoFile = File(
        context.cacheDir,
        "temp_${System.currentTimeMillis()}.jpg"
    )

    return try {
        // OutputFileOptions configures the save destination and metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outputOptions)
        // Uri converts local File path into a universal file:// URI formatted string/object
        Uri.fromFile(photoFile)
    } catch (exc: ImageCaptureException) {
        logger.error(exc) { "Taking photo failed: ${exc.message}" }
        null
    }
}
