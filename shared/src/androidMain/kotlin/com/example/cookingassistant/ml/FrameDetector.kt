package com.example.cookingassistant.ml

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy

/**
 * Common contract for frame-based detectors (MediaPipe Tasks, raw LiteRT/YOLO).
 * Implementations turn CameraX frames into [DetectionFrame] results; the camera
 * pipeline and UI stay identical regardless of the backend.
 */
interface FrameDetector {
    /** Receives one detection frame per inference; invoked on a background thread. */
    var onDetection: ((DetectionFrame) -> Unit)?

    /** Receives initialization/runtime failures; invoked on a background thread. */
    var onError: ((String) -> Unit)?

    /** Most recent upright frame, used to freeze the preview when capturing. */
    val lastPreviewBitmap: Bitmap?

    fun setup(): Boolean

    fun clear()

    fun detect(imageProxy: ImageProxy)
}
