package com.example.cookingassistant.ml

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

/**
 * Wraps MediaPipe Tasks' [ObjectDetector] for CameraX live-stream analysis.
 *
 * The model ships in the app's assets (currently EfficientDet-Lite0, trained on
 * COCO). Swapping in a food/ingredient model later only means replacing the
 * asset and [MODEL_ASSET] — the output contract stays [DetectionFrame].
 */
class MediaPipeObjectDetector(
    context: Context,
    private val maxResults: Int = DEFAULT_MAX_RESULTS,
    private val delegate: Delegate = Delegate.CPU,
) : FrameDetector {
    private val appContext = context.applicationContext

    @Volatile
    private var detector: ObjectDetector? = null

    /** Receives one detection frame per inference; invoked on MediaPipe's thread. */
    override var onDetection: ((DetectionFrame) -> Unit)? = null

    /** Receives initialization/runtime failures; invoked on a background thread. */
    override var onError: ((String) -> Unit)? = null

    /** Most recent upright frame, used to freeze the preview when capturing. */
    @Volatile
    override var lastPreviewBitmap: Bitmap? = null
        private set

    /** True when the native detector is loaded and can accept frames. */
    val isReady: Boolean get() = detector != null

    override fun setup(): Boolean {
        clear()
        return try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(MODEL_ASSET)
                .setDelegate(delegate)
                .build()
            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                // Low native floor: the user-facing threshold is applied in
                // DetectionFrame.visibleObjects so the slider never rebuilds
                // the native graph.
                .setScoreThreshold(INTERNAL_SCORE_FLOOR)
                .setMaxResults(maxResults)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, input ->
                    onDetection?.invoke(
                        result.toDetectionFrame(
                            inputWidth = input.width,
                            inputHeight = input.height,
                        )
                    )
                }
                .setErrorListener { error ->
                    onError?.invoke(error.message ?: "Unbekannter MediaPipe-Fehler")
                }
                .build()
            detector = ObjectDetector.createFromOptions(appContext, options)
            true
        } catch (e: RuntimeException) {
            Log.e(TAG, "MediaPipe object detector failed to initialise", e)
            onError?.invoke(
                "Objekterkennung konnte nicht geladen werden: ${e.message ?: "unbekannter Fehler"}"
            )
            false
        }
    }

    override fun clear() {
        detector?.close()
        detector = null
    }

    /**
     * Converts one CameraX frame (RGBA_8888) to an upright bitmap and submits it.
     * The proxy is always closed, as CameraX requires.
     */
    override fun detect(imageProxy: ImageProxy) {
        val current = detector
        if (current == null) {
            imageProxy.close()
            return
        }
        val upright = imageProxy.toUprightBitmap()
        lastPreviewBitmap = upright
        val frameTime = SystemClock.uptimeMillis()
        current.detectAsync(BitmapImageBuilder(upright).build(), frameTime)
    }

    private fun ObjectDetectorResult.toDetectionFrame(
        inputWidth: Int,
        inputHeight: Int,
    ): DetectionFrame {
        val objects = detections().map { detection ->
            val box = detection.boundingBox()
            val category = detection.categories().firstOrNull()
            DetectedObject(
                label = category?.categoryName()?.takeIf { it.isNotBlank() } ?: "Objekt",
                score = category?.score() ?: 0f,
                left = box.left,
                top = box.top,
                right = box.right,
                bottom = box.bottom,
            )
        }
        return DetectionFrame(
            objects = objects,
            inferenceTimeMs = (SystemClock.uptimeMillis() - timestampMs()).coerceAtLeast(0),
            imageWidth = inputWidth,
            imageHeight = inputHeight,
        )
    }

    companion object {
        /** Asset name; replace the file to swap in a food-specific model. */
        const val MODEL_ASSET = "efficientdet-lite0.tflite"
        const val DEFAULT_MAX_RESULTS = 5

        private const val INTERNAL_SCORE_FLOOR = 0.1f
        private const val TAG = "MediaPipeObjectDetector"
    }
}
