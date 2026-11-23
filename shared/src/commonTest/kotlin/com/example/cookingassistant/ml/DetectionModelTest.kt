package com.example.cookingassistant.ml

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetectionModelTest {

    @Test
    fun idsAreUnique() {
        val ids = AVAILABLE_DETECTION_MODELS.map { it.id }
        assertEquals(ids.size, ids.toSet().size, "duplicate model ids: $ids")
    }

    @Test
    fun defaultIdResolvesToAnEntry() {
        val default = detectionModelById(DEFAULT_DETECTION_MODEL_ID)
        assertEquals(DEFAULT_DETECTION_MODEL_ID, default.id)
    }

    @Test
    fun everyEntryHasNonBlankNameAndAssetFile() {
        AVAILABLE_DETECTION_MODELS.forEach { model ->
            assertTrue(model.displayName.isNotBlank(), "blank displayName for ${model.id}")
            assertTrue(model.assetFile.isNotBlank(), "blank assetFile for ${model.id}")
        }
    }

    @Test
    fun registryCoversBothEngines() {
        // The selector is engine-agnostic by design: both engines must appear
        // so the abstraction is exercised, not just documented.
        assertEquals(
            enumValues<DetectionEngine>().toSet(),
            AVAILABLE_DETECTION_MODELS.map { it.engine }.toSet(),
        )
    }

    @Test
    fun detectionModelByIdFallsBackToDefaultForUnknownId() {
        // Stale remembered state (e.g. a renamed registry entry) must never crash.
        assertEquals(
            detectionModelById(DEFAULT_DETECTION_MODEL_ID),
            detectionModelById("no-such-model-id"),
        )
    }
}
