package com.example.cookingassistant.ml

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Metadata Ultralytics appends to its `.tflite` exports (the `metadata.json`
 * inside the trailing ZIP): class names in index order, whether the export is
 * NMS-free (`end2end`), and the training image size.
 */
data class YoloMetadata(
    val labels: List<String>,
    val endToEnd: Boolean,
    val imgsz: List<Int>? = null,
)

/**
 * Parses the `metadata.json` Ultralytics embeds in its LiteRT exports.
 *
 * Pure Kotlin so it stays unit-testable on the host; the Android-side
 * detector only has to locate and unzip the archive.
 */
object YoloMetadataParser {

    fun parse(metadataJson: String): YoloMetadata {
        val metadata = Json.parseToJsonElement(metadataJson).jsonObject

        val labels = metadata["names"]?.jsonObject?.let { names ->
            val maxIndex = names.keys.maxOfOrNull { it.toIntOrNull() ?: -1 } ?: -1
            if (maxIndex < 0) {
                emptyList()
            } else {
                MutableList(maxIndex + 1) { "Klasse ${it + 1}" }.also { list ->
                    names.forEach { (key, value) ->
                        val index = key.toIntOrNull() ?: return@forEach
                        list[index] = value.jsonPrimitive.content
                    }
                }
            }
        } ?: emptyList()

        val endToEnd = metadata["end2end"]?.jsonPrimitive?.booleanOrNull ?: false

        val imgsz = metadata["imgsz"]?.jsonArray
            ?.takeIf { array -> array.all { it.jsonPrimitive.intOrNull != null } }
            ?.map { it.jsonPrimitive.int }

        return YoloMetadata(labels = labels, endToEnd = endToEnd, imgsz = imgsz)
    }
}

/**
 * Fallback tensor-shape derivation when the runtime cannot be asked for a
 * tensor's layout (or the answer is unavailable before the first inference).
 *
 * Ultralytics exports are fixed-shape NCHW float models, so the shapes follow
 * from the metadata alone; every inconsistency yields `null` so callers can
 * fail cleanly instead of decoding garbage.
 */
object YoloShapeHints {

    /** NCHW `[1, 3, height, width]` from the metadata image size, or null. */
    fun inputShape(imgsz: List<Int>?): IntArray? {
        if (imgsz == null || imgsz.size != 2 || imgsz.any { it <= 0 }) return null
        return intArrayOf(1, 3, imgsz[0], imgsz[1])
    }

    /**
     * Raw `[1, 4 + classes, anchors]` for the default export, or NMS-free
     * `[1, maxDet, 6]` for `end2end` exports — or null when the numbers
     * do not divide cleanly.
     */
    fun outputShape(valuesSize: Int, labelCount: Int, endToEnd: Boolean): IntArray? {
        if (valuesSize <= 0) return null
        return if (endToEnd) {
            if (valuesSize % 6 != 0) return null
            intArrayOf(1, valuesSize / 6, 6)
        } else {
            if (labelCount <= 0) return null
            val channels = 4 + labelCount
            if (valuesSize % channels != 0) return null
            intArrayOf(1, channels, valuesSize / channels)
        }
    }
}
