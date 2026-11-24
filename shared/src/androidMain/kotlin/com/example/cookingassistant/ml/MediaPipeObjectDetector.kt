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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Wraps MediaPipe Tasks' [ObjectDetector] for CameraX live-stream analysis.
 *
 * The model asset is a constructor parameter ([MODEL_ASSET] is the default), so
 * the runtime model switcher can run any MediaPipe-compatible model — a
 * food/ingredient export later joins the registry with no changes here. The
 * output contract stays [DetectionFrame].
 */
class MediaPipeObjectDetector(
    context: Context,
    private val modelAsset: String = MODEL_ASSET,
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
                .setModelAssetPath(modelAsset)
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
                    onError?.invoke(error.message ?: "Unknown MediaPipe error")
                }
                .build()
            detector = ObjectDetector.createFromOptions(appContext, options)
            Log.i(TAG, "MediaPipe object detector ready: model=$modelAsset")
            true
        } catch (e: RuntimeException) {
            Log.e(TAG, "MediaPipe object detector failed to initialise", e)
            onError?.invoke(
                "Object detection could not be loaded: ${e.message ?: "unknown error"}"
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

    /**
     * Test-support entry: submits one bitmap through the same live-stream path
     * and blocks for its result (10 s timeout). Compatible with on-device
     * instrumentation only — never call from the analyzer or main thread.
     */
    fun detectBitmap(bitmap: Bitmap): DetectionFrame? {
        val current = detector ?: return null
        val latch = CountDownLatch(1)
        var result: DetectionFrame? = null
        val previousDetection = onDetection
        val previousError = onError
        onDetection = { frame ->
            result = frame
            latch.countDown()
            previousDetection?.invoke(frame)
        }
        onError = { message ->
            latch.countDown()
            previousError?.invoke(message)
        }
        return try {
            current.detectAsync(BitmapImageBuilder(bitmap).build(), SystemClock.uptimeMillis())
            if (latch.await(10, TimeUnit.SECONDS)) result else null
        } catch (e: Exception) {
            Log.e(TAG, "detectBitmap failed", e)
            null
        } finally {
            onDetection = previousDetection
            onError = previousError
        }
    }

    private fun ObjectDetectorResult.toDetectionFrame(
        inputWidth: Int,
        inputHeight: Int,
    ): DetectionFrame {
        val objects = detections().map { detection ->
            val box = detection.boundingBox()
            val category = detection.categories().firstOrNull()
            DetectedObject(
                label = category?.categoryName()?.takeIf { it.isNotBlank() } ?: "Object",
                score = category?.score() ?: 0f,
                // MediaPipe emits boxes that can exceed the input bounds by a
                // few pixels; clamp so every engine satisfies the same
                // "boxes inside the source image" contract the UI and tests use.
                left = box.left.coerceIn(0f, inputWidth.toFloat()),
                top = box.top.coerceIn(0f, inputHeight.toFloat()),
                right = box.right.coerceIn(0f, inputWidth.toFloat()),
                bottom = box.bottom.coerceIn(0f, inputHeight.toFloat()),
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
