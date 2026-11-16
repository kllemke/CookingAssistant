package com.example.cookingassistant.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.cookingassistant.ui.theme.CameraOverlay
import com.example.cookingassistant.ui.theme.Dimens

/**
 * Shutter control drawn over the camera preview. Ring and icon use
 * [CameraOverlay] instead of a surface role: the button sits on live media,
 * not on an app surface, so colorScheme surface/onSurface roles do not apply.
 */
@Composable
fun CameraCaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Transparent,
            contentColor = CameraOverlay,
        ),
        modifier = modifier
            .size(Dimens.captureButtonSize)
            .border(Dimens.borderWidth, CameraOverlay, CircleShape)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    ) {
        Icon(
            imageVector = Icons.Default.Camera,
            contentDescription = "Foto aufnehmen",
            modifier = Modifier.size(Dimens.captureIconSize),
        )
    }
}
