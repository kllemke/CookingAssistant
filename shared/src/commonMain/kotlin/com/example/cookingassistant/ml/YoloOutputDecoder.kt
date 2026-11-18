package com.example.cookingassistant.ml

/**
 * Geometry of Ultralytics' letterbox preprocessing: the source image is scaled
 * by [scale] and placed at ([padX], [padY]) on a square, grey-padded canvas.
 */
data class LetterboxTransform(
    val scale: Float,
    val padX: Float,
    val padY: Float,
)

/**
 * Output layout of an Ultralytics YOLO LiteRT export.
 * - [NMS_FREE]: `(1, max_det, 6)` rows `[x1, y1, x2, y2, score, class]` in input pixels.
 * - [RAW]: `(1, 4 + classes, anchors)` values `[cx, cy, w, h, class scores…]` normalized.
 * - [AUTO]: guesses from the tensor shape; prefer an explicit layout.
 */
enum class YoloOutputLayout { AUTO, NMS_FREE, RAW }

/**
 * Decodes Ultralytics YOLO LiteRT outputs into [DetectedObject]s.
 *
 * Supports both layouts produced by `yolo export format=litert`:
 * - **NMS-free** `(1, max_det, 6)` rows `[x1, y1, x2, y2, score, class]` in
 *   input pixels (exported with `nms=False`, FP32 or `w8a32`);
 * - **raw** `(1, 4 + classes, anchors)` values `[cx, cy, w, h, class scores…]`
 *   normalized to 0..1 of the letterboxed input (the default export).
 *
 * In both cases boxes are in the letterboxed input space and are mapped back
 * to the original image with [LetterboxTransform].
 */
object YoloOutputDecoder {

    /** Computes the letterbox scale and padding for the given sizes. */
    fun letterbox(
        sourceWidth: Int,
        sourceHeight: Int,
        inputWidth: Int,
        inputHeight: Int,
    ): LetterboxTransform {
        if (sourceWidth <= 0 || sourceHeight <= 0 || inputWidth <= 0 || inputHeight <= 0) {
            return LetterboxTransform(1f, 0f, 0f)
        }
        val scale = minOf(
            inputWidth.toFloat() / sourceWidth,
            inputHeight.toFloat() / sourceHeight,
        )
        return LetterboxTransform(
            scale = scale,
            padX = (inputWidth - sourceWidth * scale) / 2f,
            padY = (inputHeight - sourceHeight * scale) / 2f,
        )
    }

    /**
     * Decodes one model output into detections in source-image coordinates,
     * strongest first, at most [maxResults].
     */
    fun decode(
        output: FloatArray,
        outputShape: IntArray,
        labels: List<String>,
        scoreThreshold: Float,
        iouThreshold: Float,
        maxResults: Int,
        transform: LetterboxTransform,
        sourceWidth: Int,
        sourceHeight: Int,
        inputWidth: Int,
        inputHeight: Int,
        layout: YoloOutputLayout = YoloOutputLayout.AUTO,
    ): List<DetectedObject> {
        val resolvedLayout = when (layout) {
            YoloOutputLayout.AUTO ->
                if (isNmsFreeLayout(outputShape)) YoloOutputLayout.NMS_FREE else YoloOutputLayout.RAW
            else -> layout
        }
        val candidates = when (resolvedLayout) {
            YoloOutputLayout.NMS_FREE -> decodeNmsFree(
                output = output,
                outputShape = outputShape,
                labels = labels,
                scoreThreshold = scoreThreshold,
                transform = transform,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
            )
            YoloOutputLayout.RAW -> decodeRaw(
                output = output,
                outputShape = outputShape,
                labels = labels,
                scoreThreshold = scoreThreshold,
                transform = transform,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
                inputWidth = inputWidth,
                inputHeight = inputHeight,
            )
            YoloOutputLayout.AUTO -> error("unreachable")
        }
        return nonMaxSuppression(candidates, iouThreshold).take(maxResults)
    }

    /** `(1, max_det, 6)` vs raw `(1, 4 + classes, anchors)`. */
    private fun isNmsFreeLayout(shape: IntArray): Boolean =
        shape.size == 3 && shape[2] == 6 && shape[1] in 1..1_000

    private fun decodeNmsFree(
        output: FloatArray,
        outputShape: IntArray,
        labels: List<String>,
        scoreThreshold: Float,
        transform: LetterboxTransform,
        sourceWidth: Int,
        sourceHeight: Int,
    ): List<DetectedObject> {
        val rows = outputShape[1]
        val columns = 6
        if (output.size < rows * columns) return emptyList()

        val results = ArrayList<DetectedObject>(rows)
        for (row in 0 until rows) {
            val base = row * columns
            val score = output[base + 4]
            if (score < scoreThreshold) continue

            val box = toSourceBox(
                left = output[base],
                top = output[base + 1],
                right = output[base + 2],
                bottom = output[base + 3],
                transform = transform,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
            ) ?: continue

            results += DetectedObject(
                label = labelFor(output[base + 5].toInt(), labels),
                score = score,
                left = box[0],
                top = box[1],
                right = box[2],
                bottom = box[3],
            )
        }
        return results
    }

    private fun decodeRaw(
        output: FloatArray,
        outputShape: IntArray,
        labels: List<String>,
        scoreThreshold: Float,
        transform: LetterboxTransform,
        sourceWidth: Int,
        sourceHeight: Int,
        inputWidth: Int,
        inputHeight: Int,
    ): List<DetectedObject> {
        val channels = outputShape[1]
        val anchors = outputShape[2]
        val classCount = channels - 4
        if (classCount <= 0 || output.size < channels * anchors) return emptyList()
        if (inputWidth <= 0 || inputHeight <= 0) return emptyList()

        val results = ArrayList<DetectedObject>()
        for (anchor in 0 until anchors) {
            var bestScore = 0f
            var bestClass = -1
            for (classIndex in 0 until classCount) {
                val score = output[(4 + classIndex) * anchors + anchor]
                if (score > bestScore) {
                    bestScore = score
                    bestClass = classIndex
                }
            }
            if (bestClass < 0 || bestScore < scoreThreshold) continue

            val centerX = output[anchor] * inputWidth
            val centerY = output[anchors + anchor] * inputHeight
            val width = output[2 * anchors + anchor] * inputWidth
            val height = output[3 * anchors + anchor] * inputHeight

            val box = toSourceBox(
                left = centerX - width / 2f,
                top = centerY - height / 2f,
                right = centerX + width / 2f,
                bottom = centerY + height / 2f,
                transform = transform,
                sourceWidth = sourceWidth,
                sourceHeight = sourceHeight,
            ) ?: continue

            results += DetectedObject(
                label = labelFor(bestClass, labels),
                score = bestScore,
                left = box[0],
                top = box[1],
                right = box[2],
                bottom = box[3],
            )
        }
        return results
    }

    private fun labelFor(classIndex: Int, labels: List<String>): String =
        labels.getOrNull(classIndex) ?: "Klasse ${classIndex + 1}"

    /** Maps a box from letterboxed input pixels to clamped source pixels. */
    private fun toSourceBox(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        transform: LetterboxTransform,
        sourceWidth: Int,
        sourceHeight: Int,
    ): FloatArray? {
        if (transform.scale <= 0f) return null
        val x1 = ((left - transform.padX) / transform.scale)
            .coerceIn(0f, sourceWidth.toFloat())
        val y1 = ((top - transform.padY) / transform.scale)
            .coerceIn(0f, sourceHeight.toFloat())
        val x2 = ((right - transform.padX) / transform.scale)
            .coerceIn(0f, sourceWidth.toFloat())
        val y2 = ((bottom - transform.padY) / transform.scale)
            .coerceIn(0f, sourceHeight.toFloat())
        if (x2 - x1 <= 0f || y2 - y1 <= 0f) return null
        return floatArrayOf(x1, y1, x2, y2)
    }

    /** Class-aware greedy NMS on source-space boxes. */
    private fun nonMaxSuppression(
        detections: List<DetectedObject>,
        iouThreshold: Float,
    ): List<DetectedObject> {
        if (detections.size <= 1) return detections
        val sorted = detections.sortedByDescending { it.score }
        val kept = ArrayList<DetectedObject>(sorted.size)
        for (candidate in sorted) {
            val suppressed = kept.any { existing ->
                existing.label == candidate.label &&
                    intersectionOverUnion(existing, candidate) > iouThreshold
            }
            if (!suppressed) kept += candidate
        }
        return kept
    }

    private fun intersectionOverUnion(a: DetectedObject, b: DetectedObject): Float {
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)
        val intersection = (right - left).coerceAtLeast(0f) *
            (bottom - top).coerceAtLeast(0f)
        if (intersection <= 0f) return 0f
        val areaA = (a.right - a.left) * (a.bottom - a.top)
        val areaB = (b.right - b.left) * (b.bottom - b.top)
        val union = areaA + areaB - intersection
        return if (union <= 0f) 0f else intersection / union
    }
}
