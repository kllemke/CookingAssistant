package com.example.cookingassistant.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.TensorBuffer
import com.google.ai.edge.litert.TensorType
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.math.roundToInt

/**
 * Raw LiteRT (CompiledModel) detector for Ultralytics YOLO exports.
 *
 * Runs the model exactly as exported by `yolo export format="litert"`:
 * NCHW float32 input scaled to 0..1, either a raw `(1, 4+classes, anchors)`
 * output or the NMS-free `(1, max_det, 6)` layout. Letterbox preprocessing,
 * decoding and NMS live in [YoloOutputDecoder] so they stay unit-testable.
 *
 * Tensor shapes come from the model's `serving_default` signature when the
 * runtime exposes them, with a metadata-derived fallback ([YoloShapeHints]).
 * CPU or GPU via LiteRT's CompiledModel API; the runtime falls back to CPU
 * automatically when the requested accelerator cannot run an op.
 */
class LiteRTYoloDetector(
    context: Context,
    private val modelAsset: String = MODEL_ASSET,
    private val accelerator: Accelerator = Accelerator.CPU,
    private val scoreThreshold: Float = INTERNAL_SCORE_FLOOR,
    private val iouThreshold: Float = IOU_THRESHOLD,
    private val maxResults: Int = DEFAULT_MAX_RESULTS,
) : FrameDetector {

    private val appContext = context.applicationContext

    @Volatile
    private var model: CompiledModel? = null

    private var inputBuffers: List<TensorBuffer>? = null
    private var outputBuffers: List<TensorBuffer>? = null
    private var inputWidth = 0
    private var inputHeight = 0
    private var outputShape: IntArray? = null
    private var outputShapeErrorReported = false
    private var outputLayout = YoloOutputLayout.AUTO
    private var labels: List<String> = emptyList()

    // Per-frame buffers, reused on the single analyzer thread.
    private var inputFloats: FloatArray? = null
    private var pixelBuffer: IntArray? = null
    private var letterboxBitmap: Bitmap? = null
    private val letterboxPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    override var onDetection: ((DetectionFrame) -> Unit)? = null
    override var onError: ((String) -> Unit)? = null

    override var lastPreviewBitmap: Bitmap? = null
        private set

    override fun setup(): Boolean {
        clear()
        var localModel: CompiledModel? = null
        return try {
            val metadata = loadMetadata(appContext, modelAsset)
            localModel = CompiledModel.create(
                appContext.assets,
                modelAsset,
                optionsFor(accelerator),
            )
            val (inputShape, localOutputShape) = resolveShapes(localModel, metadata)

            val localInputHeight = inputShape[2]
            val localInputWidth = inputShape[3]
            val localInputBuffers = localModel.createInputBuffers()
            val localOutputBuffers = localModel.createOutputBuffers()

            inputWidth = localInputWidth
            inputHeight = localInputHeight
            outputShape = localOutputShape
            outputShapeErrorReported = false
            labels = metadata.labels
            outputLayout = if (metadata.endToEnd) {
                YoloOutputLayout.NMS_FREE
            } else {
                YoloOutputLayout.RAW
            }
            inputBuffers = localInputBuffers
            outputBuffers = localOutputBuffers
            inputFloats = FloatArray(3 * localInputWidth * localInputHeight)
            pixelBuffer = IntArray(localInputWidth * localInputHeight)
            letterboxBitmap = Bitmap.createBitmap(
                localInputWidth,
                localInputHeight,
                Bitmap.Config.ARGB_8888,
            )
            model = localModel

            Log.i(
                TAG,
                "YOLO LiteRT ready: accelerator=$accelerator " +
                    "input=${inputShape.contentToString()} " +
                    "output=${localOutputShape?.contentToString() ?: "unknown until first run"} " +
                    "layout=$outputLayout labels=${labels.size}",
            )
            true
        } catch (e: Exception) {
            localModel?.close()
            Log.e(TAG, "YOLO LiteRT failed to initialise", e)
            onError?.invoke(
                "YOLO-Erkennung konnte nicht geladen werden: " +
                    (e.message ?: "unbekannter Fehler"),
            )
            false
        }
    }

    override fun clear() {
        model?.close()
        model = null
        inputBuffers = null
        outputBuffers = null
    }

    override fun detect(imageProxy: ImageProxy) {
        val current = model
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
        val current = model ?: return null
        return runInference(current, bitmap)
    }

    /**
     * Resolves the input/output tensor shapes: from the model's
     * `serving_default` signature when the runtime exposes it, otherwise the
     * input from the embedded metadata (and the output lazily after the
     * first inference, see [reportOutputShape]).
     */
    private fun resolveShapes(
        localModel: CompiledModel,
        metadata: YoloMetadata,
    ): Pair<IntArray, IntArray?> {
        val inputType = runCatching {
            localModel.getInputTensorType(INPUT_TENSOR_NAME, SIGNATURE_NAME)
        }.getOrNull()
        if (inputType != null) {
            require(inputType.elementType == TensorType.ElementType.FLOAT) {
                "Expected float32 input"
            }
        }
        val inputShape = inputType?.layout?.dimensions?.toIntArray()
            ?: YoloShapeHints.inputShape(metadata.imgsz)
            ?: error("Eingabegröße weder aus dem Modell noch aus den Metadaten ermittelbar")
        require(inputShape.size == 4) {
            "Expected NCHW input, got ${inputShape.contentToString()}"
        }

        val outputType = runCatching {
            localModel.getOutputTensorType(OUTPUT_TENSOR_NAME, SIGNATURE_NAME)
        }.getOrNull()
        if (outputType != null) {
            require(outputType.elementType == TensorType.ElementType.FLOAT) {
                "Expected float32 output"
            }
        }
        return inputShape to outputType?.layout?.dimensions?.toIntArray()
    }

    private fun runInference(current: CompiledModel, bitmap: Bitmap): DetectionFrame? {
        return runCatching {
            val input = inputFloats ?: return null
            val inputs = inputBuffers ?: return null
            val outputs = outputBuffers ?: return null
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
            val planeSize = inputWidth * inputHeight
            var offset = 0
            for (index in 0 until planeSize) {
                input[offset + index] = ((pixels[index] shr 16) and 0xFF) / 255f
            }
            offset += planeSize
            for (index in 0 until planeSize) {
                input[offset + index] = ((pixels[index] shr 8) and 0xFF) / 255f
            }
            offset += planeSize
            for (index in 0 until planeSize) {
                input[offset + index] = (pixels[index] and 0xFF) / 255f
            }

            inputs[0].writeFloat(input)

            val start = SystemClock.uptimeMillis()
            current.run(inputs, outputs, 0)
            val elapsed = SystemClock.uptimeMillis() - start

            val values = outputs[0].readFloat()
            val shape = reportOutputShape(values.size) ?: return null

            val objects = YoloOutputDecoder.decode(
                output = values,
                outputShape = shape,
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
            DetectionFrame(
                objects = objects,
                inferenceTimeMs = elapsed,
                imageWidth = bitmap.width,
                imageHeight = bitmap.height,
            )
        }.onFailure { e ->
            Log.e(TAG, "YOLO inference failed", e)
            onError?.invoke(
                "YOLO-Erkennung fehlgeschlagen: " + (e.message ?: "unbekannter Fehler"),
            )
        }.getOrNull()
    }

    /**
     * Returns the output shape, deriving it from the first inference's value
     * count when the runtime did not expose it. Reports once (in German) and
     * skips the frame when the numbers do not fit either export layout.
     */
    private fun reportOutputShape(valuesSize: Int): IntArray? {
        outputShape?.let { return it }
        val derived = YoloShapeHints.outputShape(
            valuesSize = valuesSize,
            labelCount = labels.size,
            endToEnd = outputLayout == YoloOutputLayout.NMS_FREE,
        )
        if (derived == null) {
            if (!outputShapeErrorReported) {
                outputShapeErrorReported = true
                Log.e(
                    TAG,
                    "Cannot derive YOLO output shape: values=$valuesSize " +
                        "labels=${labels.size} layout=$outputLayout",
                )
                onError?.invoke(
                    "YOLO-Ausgabeformat konnte nicht erkannt werden " +
                        "(${labels.size} Klassen, $valuesSize Werte)",
                )
            }
            return null
        }
        outputShape = derived
        return derived
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

        /** Ultralytics exports carry one `serving_default` signature. */
        private const val SIGNATURE_NAME = "serving_default"
        private const val INPUT_TENSOR_NAME = "serving_default_args_0"
        private const val OUTPUT_TENSOR_NAME = "output_0"

        private val EMPTY_METADATA = YoloMetadata(emptyList(), endToEnd = false, imgsz = null)

        private fun optionsFor(accelerator: Accelerator): CompiledModel.Options =
            CompiledModel.Options(accelerator).also { options ->
                if (accelerator == Accelerator.CPU) {
                    options.cpuOptions = CompiledModel.CpuOptions(numThreads = NUM_THREADS)
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
                        return YoloMetadataParser.parse(zip.readBytes().decodeToString())
                    }
                    entry = zip.nextEntry
                }
            }
            return null
        }
    }
}
