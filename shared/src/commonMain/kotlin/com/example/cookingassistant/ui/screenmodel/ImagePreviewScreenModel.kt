package com.example.cookingassistant.ui.screenmodel

import okio.FileSystem
import okio.SYSTEM
import okio.Path.Companion.toPath
import cafe.adriel.voyager.core.model.ScreenModel
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class ImagePreviewScreenModel(
) : ScreenModel {
    fun removeDiscardedImage(imagePath: String){
        val path = imagePath.toPath()
        runCatching {
            FileSystem.SYSTEM.delete(path, mustExist = false)
        }.onSuccess {
            // Runs ONLY if no exception occurred (like Python's 'else')
            logger.debug { "Image deleted successfully" }
        }.onFailure { e ->
            // Runs ONLY on exception (like 'catch')
            logger.warn(e) { "Failed to delete discarded image file at path: $imagePath" }
        }

    }
}