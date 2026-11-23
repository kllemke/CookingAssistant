package com.example.cookingassistant

import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.cookingassistant.ml.MediaPipeObjectDetector
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs the real EfficientDet-Lite0 model through MediaPipe Tasks on the device
 * and checks the adapter's bitmap → detection → label pipeline end to end.
 */
@RunWith(AndroidJUnit4::class)
class EfficientDetDetectorTest {

    @Test
    fun detectsObjectsInSampleImageOnDeviceWithDefaultAsset() {
        assertDetectorFindsObjects { MediaPipeObjectDetector(it) }
    }

    @Test
    fun detectsObjectsInSampleImageOnDeviceWithExplicitAsset() {
        // Proves the model-asset parameter the runtime model switcher relies on.
        assertDetectorFindsObjects {
            MediaPipeObjectDetector(it, modelAsset = MediaPipeObjectDetector.MODEL_ASSET)
        }
    }

    private inline fun assertDetectorFindsObjects(
        create: (android.content.Context) -> MediaPipeObjectDetector,
    ) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val appContext = instrumentation.targetContext
        val testAssets = instrumentation.context.assets

        val bitmap = testAssets.open("bus.jpg").use { stream ->
            BitmapFactory.decodeStream(stream)
        }
        assertNotNull("bus.jpg must be packaged in the test APK", bitmap)

        val detector = create(appContext)
        try {
            assertTrue("EfficientDet detector must initialise", detector.setup())

            val frame = detector.detectBitmap(bitmap!!)
            assertNotNull("Detector must return a frame", frame)
            assertTrue(
                "Expected at least one detection in bus.jpg, got none",
                frame!!.objects.isNotEmpty(),
            )
            assertTrue(
                "All scores must be within 0..1",
                frame.objects.all { it.score in 0f..1f },
            )
            assertTrue(
                "All labels must be non-blank COCO names",
                frame.objects.all { it.label.isNotBlank() && !it.label.startsWith("Klasse ") },
            )
            assertTrue(
                "Boxes must be inside the source image",
                frame.objects.all {
                    it.left >= 0f && it.top >= 0f &&
                        it.right <= frame.imageWidth + 1f &&
                        it.bottom <= frame.imageHeight + 1f
                },
            )
        } finally {
            detector.clear()
        }
    }
}
