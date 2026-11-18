package com.example.cookingassistant.ml

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy

/**
 * Converts a CameraX frame to an upright ARGB bitmap and closes the proxy.
 *
 * Expects `ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888`, where plane[0] holds
 * the RGBA pixels.
 */
internal fun ImageProxy.toUprightBitmap(): Bitmap {
    val rotationDegrees = imageInfo.rotationDegrees
    val buffer = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    use { proxy ->
        buffer.copyPixelsFromBuffer(proxy.planes[0].buffer)
    }
    if (rotationDegrees == 0) return buffer

    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
    val upright = Bitmap.createBitmap(buffer, 0, 0, buffer.width, buffer.height, matrix, true)
    if (upright !== buffer) {
        buffer.recycle()
    }
    return upright
}
