package com.example.cookingassistant.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.cookingassistant.ml.CapturedPhoto

/** Default confidence a detection must reach to be drawn on the preview. */
const val DEFAULT_CONFIDENCE_THRESHOLD = 0.5f

@Composable
expect fun CameraView(
    onImageCaptured: (CapturedPhoto) -> Unit,
    modifier: Modifier = Modifier,
    confidenceThreshold: Float = DEFAULT_CONFIDENCE_THRESHOLD,
)
