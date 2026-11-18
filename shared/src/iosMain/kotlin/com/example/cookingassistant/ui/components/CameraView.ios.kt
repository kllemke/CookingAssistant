package com.example.cookingassistant.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cookingassistant.ml.CapturedPhoto

@Composable
actual fun CameraView(
    onImageCaptured: (CapturedPhoto) -> Unit,
    modifier: Modifier,
    confidenceThreshold: Float,
) {
    TODO("CameraView is not implemented for iOS")
}
