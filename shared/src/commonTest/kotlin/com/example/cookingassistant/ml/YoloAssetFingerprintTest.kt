package com.example.cookingassistant.ml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class YoloAssetFingerprintTest {

    @Test
    fun keyIsStableForIdenticalInput() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)

        assertEquals(
            YoloAssetFingerprint.key("yolo26n.tflite", bytes),
            YoloAssetFingerprint.key("yolo26n.tflite", bytes),
        )
    }

    @Test
    fun keyChangesWhenContentChanges() {
        val original = byteArrayOf(1, 2, 3, 4, 5)
        val swapped = byteArrayOf(1, 2, 3, 4, 6) // one weight flipped

        assertNotEquals(
            YoloAssetFingerprint.key("yolo26n.tflite", original),
            YoloAssetFingerprint.key("yolo26n.tflite", swapped),
        )
    }

    @Test
    fun keyChangesWhenLengthChanges() {
        val original = byteArrayOf(1, 2, 3)
        val longer = byteArrayOf(1, 2, 3, 0)

        assertNotEquals(
            YoloAssetFingerprint.key("yolo26n.tflite", original),
            YoloAssetFingerprint.key("yolo26n.tflite", longer),
        )
    }

    @Test
    fun keyChangesWhenAssetNameChanges() {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)

        assertNotEquals(
            YoloAssetFingerprint.key("yolo26n.tflite", bytes),
            YoloAssetFingerprint.key("yolo26n_food103.tflite", bytes),
        )
    }

    @Test
    fun keyWorksForEmptyAsset() {
        // Degenerate input must still produce a usable, stable key.
        assertEquals(
            YoloAssetFingerprint.key("model.tflite", ByteArray(0)),
            YoloAssetFingerprint.key("model.tflite", ByteArray(0)),
        )
    }
}
