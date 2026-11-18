package com.example.cookingassistant.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt

/**
 * Raw LiteRT (TFLite Interpreter) detector for Ultralytics YOLO exports.
 *
 * Runs the model exactly as exported by `yolo export format="litert"`:
 * NCHW float32 input scaled to 0..1, either a raw `(1, 4+classes, anchors)`
 * output or the NMS-free `(1, max_det, 6)` layout. Letterbox preprocessing,
 * decoding and NMS live in [YoloOutputDecoder] so they stay unit-testable.
 *
 * CPU-only on LiteRT v2's Interpreter API; GPU/NPU would use CompiledModel.
 */
class LiteRTYoloDetector(
    context: Context,
    private val modelAsset: String = MODEL_ASSET,
    private val scoreThreshold: Float = INTERNAL_SCORE_FLOOR,
    private val iouThreshold: Float = IOU_THRESHOLD,
    private val maxResults: Int = DEFAULT_MAX_RESULTS,
) : FrameDetector {

    private val appContext = context.applicationContext

    @Volatile
    private var interpreter: Interpreter? = null

    private var inputWidth = 0
    private var inputHeight = 0
    private var outputShape = IntArray(0)
    private var outputLayout = YoloOutputLayout.AUTO
    private var labels: List<String> = emptyList()

    // Per-frame buffers, reused on the single analyzer thread.
    private var inputBuffer: ByteBuffer? = null
    private var outputBuffer: ByteBuffer? = null
    private var outputValues: FloatArray? = null
    private var pixelBuffer: IntArray? = null
    private var letterboxBitmap: Bitmap? = null
    private val letterboxPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    override var onDetection: ((DetectionFrame) -> Unit)? = null
    override var onError: ((String) -> Unit)? = null

    override var lastPreviewBitmap: Bitmap? = null
        private set

    override fun setup(): Boolean {
        clear()
        return try {
            val model = loadModel(appContext, modelAsset)
            val localInterpreter = Interpreter(
                model,
                Interpreter.Options().setNumThreads(NUM_THREADS),
            )
            val inputShape = localInterpreter.getInputTensor(0).shape()
            val localOutputShape = localInterpreter.getOutputTensor(0).shape()
            require(inputShape.size == 4) {
                "Expected NCHW input, got ${inputShape.contentToString()}"
            }
            require(localInterpreter.getInputTensor(0).dataType() == DataType.FLOAT32) {
                "Expected float32 input"
            }
            require(localInterpreter.getOutputTensor(0).dataType() == DataType.FLOAT32) {
                "Expected float32 output"
            }

            val localInputHeight = inputShape[2]
            val localInputWidth = inputShape[3]
            val outputElements = localOutputShape.fold(1) { total, value -> total * value }

            inputBuffer = ByteBuffer
                .allocateDirect(localInputWidth * localInputHeight * 3 * 4)
                .order(ByteOrder.nativeOrder())
            outputBuffer = ByteBuffer
                .allocateDirect(outputElements * 4)
                .order(ByteOrder.nativeOrder())
            outputValues = FloatArray(outputElements)
            pixelBuffer = IntArray(localInputWidth * localInputHeight)
            letterboxBitmap = Bitmap.createBitmap(
                localInputWidth,
                localInputHeight,
                Bitmap.Config.ARGB_8888,
            )

            inputWidth = localInputWidth
            inputHeight = localInputHeight
            outputShape = localOutputShape
            val metadata = loadMetadata(appContext, modelAsset)
            labels = metadata.labels
            outputLayout = if (metadata.endToEnd) {
                YoloOutputLayout.NMS_FREE
            } else {
                YoloOutputLayout.RAW
            }
            interpreter = localInterpreter

            Log.i(
                TAG,
                "YOLO LiteRT ready: input=${inputShape.contentToString()} " +
                    "output=${localOutputShape.contentToString()} layout=$outputLayout " +
                    "labels=${labels.size}",
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "YOLO LiteRT failed to initialise", e)
            onError?.invoke(
                "YOLO-Erkennung konnte nicht geladen werden: " +
                    (e.message ?: "unbekannter Fehler")
            )
            false
        }
    }

    override fun clear() {
        interpreter?.close()
        interpreter = null
    }

    override fun detect(imageProxy: ImageProxy) {
        val current = interpreter
        if (current == null) {
            imageProxy.close()
            return
        }
        val bitmap = imageProxy.toUprightBitmap()
        lastPreviewBitmap = bitmap
        val frame = runInference(current, bitmap) ?: return
        onDetection?.invoke(frame)
    }

    /** Runs one still-image inference; used by on-device tests. */
    fun detectBitmap(bitmap: Bitmap): DetectionFrame? {
        val current = interpreter ?: return null
        return runInference(current, bitmap)
    }

    private fun runInference(current: Interpreter, bitmap: Bitmap): DetectionFrame? {
        val input = inputBuffer ?: return null
        val output = outputBuffer ?: return null
        val values = outputValues ?: return null
        val pixels = pixelBuffer ?: return null
        val letterboxed = letterboxBitmap ?: return null

        val transform = YoloOutputDecoder.letterbox(
            sourceWidth = bitmap.width,
            sourceHeight = bitmap.height,
            inputWidth = inputWidth,
            inputHeight = inputHeight,
        )
        fillLetterbox(bitmap, letterboxed, transform)
        letterboxed.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        // NCHW: three float planes, RGB, 0..1.
        input.rewind()
        val planeSize = inputWidth * inputHeight
        for (index in 0 until planeSize) {
            input.putFloat(((pixels[index] shr 16) and 0xFF) / 255f)
        }
        for (index in 0 until planeSize) {
            input.putFloat(((pixels[index] shr 8) and 0xFF) / 255f)
        }
        for (index in 0 until planeSize) {
            input.putFloat((pixels[index] and 0xFF) / 255f)
        }
        input.rewind()
        output.rewind()

        val start = SystemClock.uptimeMillis()
        current.run(input, output)
        val elapsed = SystemClock.uptimeMillis() - start

        output.rewind()
        output.asFloatBuffer().get(values)

        val objects = YoloOutputDecoder.decode(
            output = values,
            outputShape = outputShape,
            labels = labels,
            scoreThreshold = scoreThreshold,
            iouThreshold = iouThreshold,
            maxResults = maxResults,
            transform = transform,
            sourceWidth = bitmap.width,
            sourceHeight = bitmap.height,
            inputWidth = inputWidth,
            inputHeight = inputHeight,
            layout = outputLayout,
        )
        return DetectionFrame(
            objects = objects,
            inferenceTimeMs = elapsed,
            imageWidth = bitmap.width,
            imageHeight = bitmap.height,
        )
    }

    /** Draws the source image onto a grey 114-padded square canvas. */
    private fun fillLetterbox(
        source: Bitmap,
        target: Bitmap,
        transform: LetterboxTransform,
    ) {
        val canvas = Canvas(target)
        canvas.drawColor(Color.rgb(114, 114, 114))
        val scaledWidth = (source.width * transform.scale).roundToInt().coerceAtLeast(1)
        val scaledHeight = (source.height * transform.scale).roundToInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true)
        canvas.drawBitmap(scaled, transform.padX, transform.padY, letterboxPaint)
        if (scaled !== source) {
            scaled.recycle()
        }
    }

    companion object {
        /** Asset name; replace the file to swap in a food-specific YOLO export. */
        const val MODEL_ASSET = "yolo26n.tflite"
        const val DEFAULT_MAX_RESULTS = 5

        /**
         * Native score floor; the user-facing threshold is applied in
         * [DetectionFrame.visibleObjects], as with the MediaPipe backend.
         */
        private const val INTERNAL_SCORE_FLOOR = 0.1f
        private const val IOU_THRESHOLD = 0.45f
        private const val NUM_THREADS = 4
        private const val TAG = "LiteRTYoloDetector"

        private fun loadModel(context: Context, asset: String): MappedByteBuffer =
            context.assets.openFd(asset).use { descriptor ->
                FileInputStream(descriptor.fileDescriptor).channel.use { channel ->
                    channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        descriptor.startOffset,
                        descriptor.declaredLength,
                    )
                }
            }

        /**
         * Ultralytics appends a ZIP containing `metadata.json` (class names,
         * task, imgsz, end2end…) to the `.tflite` flatbuffer. Java's
         * ZipInputStream cannot read such a prefixed archive from byte 0, so
         * locate the first local file header and parse from there.
         */
        private fun loadMetadata(context: Context, asset: String): YoloMetadata {
            val bytes = runCatching {
                context.assets.open(asset).use { it.readBytes() }
            }.getOrNull() ?: return EMPTY_METADATA

            var searchFrom = 0
            while (searchFrom <= bytes.size - 4) {
                val start = indexOfLocalHeader(bytes, searchFrom)
                if (start < 0) break
                val parsed = runCatching { parseZipAt(bytes, start) }.getOrNull()
                if (parsed != null) return parsed
                searchFrom = start + 4
            }
            return EMPTY_METADATA
        }

        private fun indexOfLocalHeader(bytes: ByteArray, from: Int): Int {
            for (index in from..bytes.size - 4) {
                if (bytes[index] == 'P'.code.toByte() &&
                    bytes[index + 1] == 'K'.code.toByte() &&
                    bytes[index + 2] == 0x03.toByte() &&
                    bytes[index + 3] == 0x04.toByte()
                ) {
                    return index
                }
            }
            return -1
        }

        private fun parseZipAt(bytes: ByteArray, start: Int): YoloMetadata? {
            ZipInputStream(
                ByteArrayInputStream(bytes, start, bytes.size - start),
            ).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name.endsWith("metadata.json")) {
                        return parseMetadata(zip.readBytes().decodeToString())
                    }
                    entry = zip.nextEntry
                }
            }
            return null
        }

        private fun parseMetadata(metadataJson: String): YoloMetadata {
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
            return YoloMetadata(labels, endToEnd)
        }

        private data class YoloMetadata(
            val labels: List<String>,
            val endToEnd: Boolean,
        )

        private val EMPTY_METADATA = YoloMetadata(emptyList(), endToEnd = false)
    }
}
