package com.example.cookingassistant.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookingassistant.ml.DetectionFrame
import com.example.cookingassistant.ui.theme.CameraOverlay
import com.example.cookingassistant.ui.theme.Ink
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Draws detection boxes over the camera preview.
 *
 * The preview is full-bleed (PreviewView FILL_CENTER), so image coordinates are
 * mapped with a cover scale and centered crop offsets.
 */
@Composable
fun DetectionOverlay(
    frame: DetectionFrame,
    confidenceThreshold: Float,
    modifier: Modifier = Modifier,
) {
    val objects = frame.visibleObjects(confidenceThreshold)
    val textMeasurer = rememberTextMeasurer()
    val boxColor = MaterialTheme.colorScheme.primary
    val labelBackground = Ink
    val labelText = CameraOverlay

    Canvas(modifier = modifier.testTag("detection_overlay")) {
        if (objects.isEmpty()) return@Canvas

        val imageWidth = frame.imageWidth.coerceAtLeast(1).toFloat()
        val imageHeight = frame.imageHeight.coerceAtLeast(1).toFloat()
        val scale = max(size.width / imageWidth, size.height / imageHeight)
        val offsetX = (size.width - imageWidth * scale) / 2f
        val offsetY = (size.height - imageHeight * scale) / 2f
        val strokeWidth = 2.5.dp.toPx()
        val cornerPx = 8.dp.toPx()
        val pillPadH = 6.dp.toPx()
        val pillPadV = 3.dp.toPx()

        objects.forEach { obj ->
            val left = offsetX + obj.left * scale
            val top = offsetY + obj.top * scale
            val boxWidth = (obj.right - obj.left) * scale
            val boxHeight = (obj.bottom - obj.top) * scale

            drawRoundRect(
                color = boxColor,
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = strokeWidth),
            )

            val caption = "${obj.label} ${(obj.score * 100f).roundToInt()} %"
            val measured = textMeasurer.measure(
                text = caption,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            val pillWidth = measured.size.width + pillPadH * 2f
            val pillHeight = measured.size.height + pillPadV * 2f
            val pillX = left.coerceIn(0f, (size.width - pillWidth).coerceAtLeast(0f))
            val pillY = if (top - pillHeight >= 0f) {
                top - pillHeight
            } else {
                (top + strokeWidth).coerceAtMost((size.height - pillHeight).coerceAtLeast(0f))
            }

            drawRoundRect(
                color = labelBackground,
                topLeft = Offset(pillX, pillY),
                size = Size(pillWidth, pillHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
            )
            drawText(
                textLayoutResult = measured,
                color = labelText,
                topLeft = Offset(pillX + pillPadH, pillY + pillPadV),
            )
        }
    }
}
