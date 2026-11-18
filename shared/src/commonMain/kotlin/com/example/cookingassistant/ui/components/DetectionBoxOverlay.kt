package com.example.cookingassistant.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cookingassistant.ml.NormalizedDetection
import com.example.cookingassistant.ui.theme.CameraOverlay
import com.example.cookingassistant.ui.theme.Ink
import kotlin.math.roundToInt

/**
 * Draws detection boxes whose bounds are normalized (0..1) over the full
 * composable bounds. Used on the captured-photo preview, where the image is
 * stretched to fill the box (ContentScale.FillBounds).
 */
@Composable
fun DetectionBoxOverlay(
    detections: List<NormalizedDetection>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val boxColor = MaterialTheme.colorScheme.primary
    val labelBackground = Ink
    val labelText = CameraOverlay

    Canvas(modifier = modifier) {
        if (detections.isEmpty()) return@Canvas

        val strokeWidth = 2.5.dp.toPx()
        val cornerPx = 8.dp.toPx()
        val pillPadH = 6.dp.toPx()
        val pillPadV = 3.dp.toPx()

        detections.forEach { detection ->
            val left = detection.left * size.width
            val top = detection.top * size.height
            val boxWidth = (detection.right - detection.left) * size.width
            val boxHeight = (detection.bottom - detection.top) * size.height

            drawRoundRect(
                color = boxColor,
                topLeft = Offset(left, top),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = strokeWidth),
            )

            val caption = "${detection.label} ${(detection.score * 100f).roundToInt()} %"
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
