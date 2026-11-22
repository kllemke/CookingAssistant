package com.example.cookingassistant.ml

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class YoloMetadataTest {

    private val threeClassMetadata = """
        {
          "format": "litert",
          "imgsz": [640, 640],
          "names": {"0": "candy", "1": "egg tart", "2": "french fries"},
          "task": "detect",
          "end2end": false
        }
    """.trimIndent()

    @Test
    fun parseReadsLabelsEndToEndAndImgsz() {
        val metadata = YoloMetadataParser.parse(threeClassMetadata)

        assertEquals(listOf("candy", "egg tart", "french fries"), metadata.labels)
        assertFalse(metadata.endToEnd)
        assertEquals(listOf(640, 640), metadata.imgsz)
    }

    @Test
    fun parseReadsEndToEndTrue() {
        val metadata = YoloMetadataParser.parse(
            threeClassMetadata.replace("\"end2end\": false", "\"end2end\": true"),
        )

        assertTrue(metadata.endToEnd)
    }

    @Test
    fun parseWithoutNamesYieldsEmptyLabels() {
        val metadata = YoloMetadataParser.parse("""{"task": "detect", "imgsz": [640, 640]}""")

        assertTrue(metadata.labels.isEmpty())
    }

    @Test
    fun parseFillsSparseNameIndexesWithGermanPlaceholder() {
        val metadata = YoloMetadataParser.parse(
            """{"names": {"0": "candy", "2": "french fries"}, "imgsz": [640, 640]}""",
        )

        assertEquals(listOf("candy", "Klasse 2", "french fries"), metadata.labels)
    }

    @Test
    fun parseWithoutImgszYieldsNull() {
        val metadata = YoloMetadataParser.parse("""{"names": {"0": "candy"}}""")

        assertNull(metadata.imgsz)
    }

    @Test
    fun inputShapeBuildsSquareNchwFromImgsz() {
        assertContentEquals(
            intArrayOf(1, 3, 640, 640),
            YoloShapeHints.inputShape(listOf(640, 640)),
        )
        assertContentEquals(
            intArrayOf(1, 3, 1280, 1280),
            YoloShapeHints.inputShape(listOf(1280, 1280)),
        )
    }

    @Test
    fun inputShapeRejectsMissingOrMalformedImgsz() {
        assertNull(YoloShapeHints.inputShape(null))
        assertNull(YoloShapeHints.inputShape(listOf(640)))
        assertNull(YoloShapeHints.inputShape(listOf(640, 0)))
        assertNull(YoloShapeHints.inputShape(listOf(640, -3)))
    }

    @Test
    fun outputShapeDerivesRawLayout() {
        assertContentEquals(
            intArrayOf(1, 7, 8400),
            YoloShapeHints.outputShape(valuesSize = 8400 * 7, labelCount = 3, endToEnd = false),
        )
    }

    @Test
    fun outputShapeDerivesNmsFreeLayout() {
        assertContentEquals(
            intArrayOf(1, 300, 6),
            YoloShapeHints.outputShape(valuesSize = 6 * 300, labelCount = 3, endToEnd = true),
        )
    }

    @Test
    fun outputShapeRejectsInconsistentSizes() {
        assertNull(YoloShapeHints.outputShape(valuesSize = 8400 * 7, labelCount = 0, endToEnd = false))
        assertNull(YoloShapeHints.outputShape(valuesSize = 8400 * 7 + 1, labelCount = 3, endToEnd = false))
        assertNull(YoloShapeHints.outputShape(valuesSize = 6 * 300 + 1, labelCount = 3, endToEnd = true))
    }
}
