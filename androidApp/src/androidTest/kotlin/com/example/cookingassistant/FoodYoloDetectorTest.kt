package com.example.cookingassistant

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cookingassistant.ml.AVAILABLE_DETECTION_MODELS
import com.example.cookingassistant.ml.LiteRTYoloDetector
import com.example.cookingassistant.ml.YoloMetadataParser
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

/**
 * Runs the fine-tuned food YOLO26n through the same detector the camera uses,
 * constructing it from the model registry entry the selector offers — proving
 * the entry is loadable end to end — and checks that every label comes from
 * the model's own metadata rather than the COCO label set.
 */
@RunWith(AndroidJUnit4::class)
class FoodYoloDetectorTest {

    @Test
    fun detectsFoodWithLabelsFromModelMetadata() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val appContext = instrumentation.targetContext
        val testAssets = instrumentation.context.assets

        val food = AVAILABLE_DETECTION_MODELS.first { it.id == "yolo26n-food103" }

        val bitmap = testAssets.open("food.jpg").use { stream ->
            BitmapFactory.decodeStream(stream)
        }
        assertNotNull("food.jpg must be packaged in the test APK", bitmap)

        val detector = LiteRTYoloDetector(appContext, modelAsset = food.assetFile)
        try {
            assertTrue("Food detector must initialise", detector.setup())

            val frame = detector.detectBitmap(bitmap!!)
            assertNotNull("Detector must return a frame", frame)
            assertTrue(
                "Expected at least one food detection in food.jpg",
                frame!!.objects.isNotEmpty(),
            )

            val assetBytes = appContext.assets.open(food.assetFile).use { it.readBytes() }
            val expectedLabels = labelsFromModelMetadata(assetBytes)

            assertTrue(
                "All scores must be within 0..1",
                frame.objects.all { it.score in 0f..1f },
            )
            assertTrue(
                "Boxes must be inside the source image",
                frame.objects.all {
                    it.left >= 0f && it.top >= 0f &&
                        it.right <= frame.imageWidth + 1f &&
                        it.bottom <= frame.imageHeight + 1f
                },
            )
            // Labels must come from the food model's own metadata — a COCO-label
            // regression (e.g. "person") is not in this set and fails here.
            assertTrue(
                "Every label must be a class of the food model, got " +
                    frame.objects.map { it.label },
                frame.objects.all { it.label in expectedLabels },
            )
            assertTrue(
                "All labels must be non-blank real class names",
                frame.objects.all { it.label.isNotBlank() && !it.label.startsWith("Klasse ") },
            )
        } finally {
            detector.clear()
        }
    }

    /**
     * Reads the Ultralytics `metadata.json` the export appends as a ZIP after
     * the flatbuffer: locate the first local file header, then unzip from
     * there (the same trick the detector uses).
     */
    private fun labelsFromModelMetadata(assetBytes: ByteArray): Set<String> {
        for (index in 0..assetBytes.size - 4) {
            val isLocalHeader = assetBytes[index] == 'P'.code.toByte() &&
                assetBytes[index + 1] == 'K'.code.toByte() &&
                assetBytes[index + 2] == 0x03.toByte() &&
                assetBytes[index + 3] == 0x04.toByte()
            if (!isLocalHeader) continue
            ZipInputStream(ByteArrayInputStream(assetBytes.copyOfRange(index, assetBytes.size)))
                .use { zip ->
                    val entry = zip.nextEntry ?: return emptySet()
                    if (entry.name == "metadata.json") {
                        val json = zip.readBytes().toString(Charsets.UTF_8)
                        return YoloMetadataParser.parse(json).labels.toSet()
                    }
                }
        }
        return emptySet()
    }
}
