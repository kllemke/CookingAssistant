package com.example.cookingassistant.ml

/**
 * Content-derived cache key for a model asset.
 *
 * The detector's documented swap path is "replace the file, keep the asset
 * name" — so the GPU program cache must not be keyed by the asset name
 * alone: after a swap it would reuse programs compiled for the old weights.
 * The key mixes the name with the byte length and an FNV-1a hash of the
 * content, so any change to the file produces a fresh cache entry. Pure
 * Kotlin (no `java.util.zip`) to stay usable from every target.
 */
object YoloAssetFingerprint {

    fun key(assetName: String, bytes: ByteArray): String {
        var hash = 0xcbf29ce484222325UL
        for (byte in bytes) {
            hash = (hash xor (byte.toUByte().toULong())) * 0x100000001b3UL
        }
        return "$assetName-${bytes.size}-${hash.toString(16)}"
    }
}
