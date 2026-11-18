package com.example.cookingassistant.ml

/**
 * One detected object in upright image coordinates (pixels, origin top-left).
 * Pure data so it can be produced by platform code and consumed by shared UI.
 */
data class DetectedObject(
    val label: String,
    val score: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/**
 * A detection whose bounds are normalized to 0..1 of the image dimensions, so
 * a composable can overlay it on any photo regardless of display size.
 */
data class NormalizedDetection(
    val label: String,
    val score: Float,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
)

/**
 * Result of a single detection pass.
 *
 * [imageWidth] and [imageHeight] describe the coordinate space of the box
 * bounds; the UI scales them onto the preview surface.
 */
data class DetectionFrame(
    val objects: List<DetectedObject>,
    val inferenceTimeMs: Long,
    val imageWidth: Int,
    val imageHeight: Int,
) {
    /**
     * Objects at or above [threshold], strongest first. The user-facing
     * confidence setting is applied here rather than inside the native
     * detector so moving the slider does not rebuild the model.
     */
    fun visibleObjects(threshold: Float): List<DetectedObject> =
        objects.filter { it.score >= threshold }.sortedByDescending { it.score }
}

/**
 * Detections above [threshold] converted to 0..1 bounds, strongest first.
 */
fun DetectionFrame.normalizedDetections(threshold: Float): List<NormalizedDetection> {
    val width = imageWidth.coerceAtLeast(1).toFloat()
    val height = imageHeight.coerceAtLeast(1).toFloat()
    return visibleObjects(threshold).map { obj ->
        NormalizedDetection(
            label = obj.label,
            score = obj.score,
            left = (obj.left / width).coerceIn(0f, 1f),
            top = (obj.top / height).coerceIn(0f, 1f),
            right = (obj.right / width).coerceIn(0f, 1f),
            bottom = (obj.bottom / height).coerceIn(0f, 1f),
        )
    }
}

/**
 * A photo captured by the camera together with the detections that were
 * visible at shutter time.
 */
data class CapturedPhoto(
    val path: String,
    val detections: List<NormalizedDetection>,
)
