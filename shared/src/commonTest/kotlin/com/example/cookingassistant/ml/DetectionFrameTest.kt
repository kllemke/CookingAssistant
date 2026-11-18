package com.example.cookingassistant.ml

import kotlin.test.Test
import kotlin.test.assertEquals

class DetectionFrameTest {

    private fun objectWith(
        label: String,
        score: Float,
        left: Float = 0f,
        top: Float = 0f,
        right: Float = 10f,
        bottom: Float = 10f,
    ) = DetectedObject(
        label = label,
        score = score,
        left = left,
        top = top,
        right = right,
        bottom = bottom,
    )

    private fun frameOf(vararg objects: DetectedObject) = DetectionFrame(
        objects = objects.toList(),
        inferenceTimeMs = 30,
        imageWidth = 640,
        imageHeight = 480,
    )

    @Test
    fun visibleObjectsKeepsObjectsAtOrAboveThreshold() {
        val frame = frameOf(
            objectWith("Person", 0.49f),
            objectWith("Tomate", 0.5f),
            objectWith("Apfel", 0.91f),
        )

        val visible = frame.visibleObjects(0.5f)

        assertEquals(listOf("Apfel", "Tomate"), visible.map { it.label })
    }

    @Test
    fun visibleObjectsSortsStrongestFirst() {
        val frame = frameOf(
            objectWith("A", 0.55f),
            objectWith("B", 0.99f),
            objectWith("C", 0.70f),
        )

        assertEquals(
            listOf("B", "C", "A"),
            frame.visibleObjects(0.5f).map { it.label },
        )
    }

    @Test
    fun visibleObjectsReturnsEmptyWhenNothingClearsThreshold() {
        val frame = frameOf(objectWith("A", 0.2f))

        assertEquals(emptyList(), frame.visibleObjects(0.5f))
    }

    @Test
    fun normalizedDetectionsDivideByImageSize() {
        val frame = frameOf(
            objectWith(
                label = "Tomate",
                score = 0.9f,
                left = 64f,
                top = 48f,
                right = 320f,
                bottom = 240f,
            ),
        )

        val normalized = frame.normalizedDetections(0.5f).single()

        assertEquals(0.1f, normalized.left)
        assertEquals(0.1f, normalized.top)
        assertEquals(0.5f, normalized.right)
        assertEquals(0.5f, normalized.bottom)
    }

    @Test
    fun normalizedDetectionsRespectThresholdAndOrder() {
        val frame = frameOf(
            objectWith("A", 0.3f, right = 640f, bottom = 480f),
            objectWith("B", 0.8f, right = 640f, bottom = 480f),
            objectWith("C", 0.95f, right = 640f, bottom = 480f),
        )

        val normalized = frame.normalizedDetections(0.5f)

        assertEquals(listOf("C", "B"), normalized.map { it.label })
        assertEquals(1f, normalized.first().right)
        assertEquals(1f, normalized.first().bottom)
    }
}
