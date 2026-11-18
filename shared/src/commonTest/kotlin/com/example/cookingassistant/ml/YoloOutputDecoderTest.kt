package com.example.cookingassistant.ml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class YoloOutputDecoderTest {

    private val labels = listOf("person", "bicycle", "car")

    private data class RawAnchor(
        val cx: Float,
        val cy: Float,
        val width: Float,
        val height: Float,
        val scores: FloatArray,
    )

    /** Builds the raw Ultralytics layout: (1, 4 + classes, anchors). */
    private fun rawOutput(
        classCount: Int,
        anchors: List<RawAnchor>,
    ): Pair<FloatArray, IntArray> {
        val channels = 4 + classCount
        val anchorCount = anchors.size
        val output = FloatArray(channels * anchorCount)
        anchors.forEachIndexed { anchor, value ->
            output[anchor] = value.cx
            output[anchorCount + anchor] = value.cy
            output[2 * anchorCount + anchor] = value.width
            output[3 * anchorCount + anchor] = value.height
            value.scores.forEachIndexed { classIndex, score ->
                output[(4 + classIndex) * anchorCount + anchor] = score
            }
        }
        return output to intArrayOf(1, channels, anchorCount)
    }

    @Test
    fun letterboxCentersFourByThreeSourceVertically() {
        val transform = YoloOutputDecoder.letterbox(640, 480, 640, 640)

        assertEquals(1f, transform.scale)
        assertEquals(0f, transform.padX)
        assertEquals(80f, transform.padY)
    }

    @Test
    fun letterboxScalesSixteenByNineSource() {
        val transform = YoloOutputDecoder.letterbox(1280, 720, 640, 640)

        assertEquals(0.5f, transform.scale)
        assertEquals(0f, transform.padX)
        assertEquals(140f, transform.padY)
    }

    @Test
    fun decodesRawAnchorAndMapsBackFromLetterbox() {
        val anchors = listOf(
            RawAnchor(
                cx = 0.5f,
                cy = 0.5f,
                width = 0.2f,
                height = 0.2f,
                scores = floatArrayOf(0.9f, 0.1f, 0.05f),
            ),
        )
        val (output, shape) = rawOutput(classCount = 3, anchors = anchors)

        val detections = YoloOutputDecoder.decode(
            output = output,
            outputShape = shape,
            labels = labels,
            scoreThreshold = 0.25f,
            iouThreshold = 0.45f,
            maxResults = 5,
            transform = YoloOutputDecoder.letterbox(640, 480, 640, 640),
            sourceWidth = 640,
            sourceHeight = 480,
            inputWidth = 640,
            inputHeight = 640,
            layout = YoloOutputLayout.RAW,
        )

        val detection = detections.single()
        assertEquals("person", detection.label)
        assertEquals(0.9f, detection.score)
        // input box x 256..384, y 256..384; minus 80px top padding
        assertEquals(256f, detection.left)
        assertEquals(176f, detection.top)
        assertEquals(384f, detection.right)
        assertEquals(304f, detection.bottom)
    }

    @Test
    fun rawDecodeDropsAnchorsBelowThreshold() {
        val anchors = listOf(
            RawAnchor(0.5f, 0.5f, 0.2f, 0.2f, floatArrayOf(0.2f, 0.1f, 0.05f)),
        )
        val (output, shape) = rawOutput(classCount = 3, anchors = anchors)

        val detections = YoloOutputDecoder.decode(
            output = output,
            outputShape = shape,
            labels = labels,
            scoreThreshold = 0.25f,
            iouThreshold = 0.45f,
            maxResults = 5,
            transform = YoloOutputDecoder.letterbox(640, 640, 640, 640),
            sourceWidth = 640,
            sourceHeight = 640,
            inputWidth = 640,
            inputHeight = 640,
            layout = YoloOutputLayout.RAW,
        )

        assertTrue(detections.isEmpty())
    }

    @Test
    fun suppressesOverlappingBoxesOfTheSameClassOnly() {
        val anchors = listOf(
            RawAnchor(0.50f, 0.50f, 0.4f, 0.4f, floatArrayOf(0.90f, 0.0f, 0.0f)),
            RawAnchor(0.51f, 0.51f, 0.4f, 0.4f, floatArrayOf(0.80f, 0.0f, 0.0f)),
            RawAnchor(0.50f, 0.50f, 0.4f, 0.4f, floatArrayOf(0.0f, 0.70f, 0.0f)),
        )
        val (output, shape) = rawOutput(classCount = 3, anchors = anchors)

        val detections = YoloOutputDecoder.decode(
            output = output,
            outputShape = shape,
            labels = labels,
            scoreThreshold = 0.25f,
            iouThreshold = 0.45f,
            maxResults = 5,
            transform = YoloOutputDecoder.letterbox(640, 640, 640, 640),
            sourceWidth = 640,
            sourceHeight = 640,
            inputWidth = 640,
            inputHeight = 640,
            layout = YoloOutputLayout.RAW,
        )

        assertEquals(listOf("person", "bicycle"), detections.map { it.label })
    }

    @Test
    fun decodesNmsFreeRowsInInputPixels() {
        val output = floatArrayOf(
            100f, 100f, 200f, 200f, 0.90f, 0f,
            10f, 10f, 20f, 20f, 0.10f, 1f,
        )
        val shape = intArrayOf(1, 2, 6)

        val detections = YoloOutputDecoder.decode(
            output = output,
            outputShape = shape,
            labels = labels,
            scoreThreshold = 0.25f,
            iouThreshold = 0.45f,
            maxResults = 5,
            transform = YoloOutputDecoder.letterbox(640, 480, 640, 640),
            sourceWidth = 640,
            sourceHeight = 480,
            inputWidth = 640,
            inputHeight = 640,
            layout = YoloOutputLayout.NMS_FREE,
        )

        val detection = detections.single()
        assertEquals("person", detection.label)
        assertEquals(100f, detection.left)
        assertEquals(20f, detection.top)
        assertEquals(200f, detection.right)
        assertEquals(120f, detection.bottom)
    }

    @Test
    fun limitsResultsToMaxResults() {
        val anchors = (0 until 6).map { index ->
            RawAnchor(
                cx = 0.05f + index * 0.15f,
                cy = 0.5f,
                width = 0.05f,
                height = 0.1f,
                scores = floatArrayOf(0.9f - index * 0.05f, 0f, 0f),
            )
        }
        val (output, shape) = rawOutput(classCount = 3, anchors = anchors)

        val detections = YoloOutputDecoder.decode(
            output = output,
            outputShape = shape,
            labels = labels,
            scoreThreshold = 0.25f,
            iouThreshold = 0.45f,
            maxResults = 3,
            transform = YoloOutputDecoder.letterbox(640, 640, 640, 640),
            sourceWidth = 640,
            sourceHeight = 640,
            inputWidth = 640,
            inputHeight = 640,
            layout = YoloOutputLayout.RAW,
        )

        assertEquals(3, detections.size)
        assertEquals(detections.map { it.score }, detections.map { it.score }.sortedDescending())
    }
}
