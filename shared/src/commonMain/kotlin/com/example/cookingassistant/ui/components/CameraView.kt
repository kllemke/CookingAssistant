package com.example.cookingassistant.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraView(
    onImageCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
)