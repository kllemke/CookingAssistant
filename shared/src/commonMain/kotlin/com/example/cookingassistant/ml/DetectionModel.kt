package com.example.cookingassistant.ml

/**
 * Engine that executes a detection model. An implementation detail of the
 * registry entry: the user picks models, never engines.
 */
enum class DetectionEngine {
    /** Raw LiteRT (`CompiledModel`) with GPU acceleration. */
    LITERT,

    /** MediaPipe Tasks Vision ObjectDetector. */
    MEDIAPIPE,
}

/** One selectable detection model: what the settings sheet lists and the camera runs. */
data class DetectionModel(
    val id: String,
    val displayName: String,
    val assetFile: String,
    val engine: DetectionEngine,
)

/**
 * Models bundled in the app. A newly trained model joins as one entry here,
 * with its asset committed alongside — the fine-tuned food YOLO26n and the
 * fine-tuned food EfficientDet are each a single line, engine included.
 */
val AVAILABLE_DETECTION_MODELS: List<DetectionModel> = listOf(
    DetectionModel(
        id = "yolo26n-coco",
        displayName = "YOLO26n (COCO)",
        assetFile = "yolo26n.tflite",
        engine = DetectionEngine.LITERT,
    ),
    DetectionModel(
        id = "efficientdet-lite0-coco",
        displayName = "EfficientDet-L0 (COCO)",
        assetFile = "efficientdet-lite0.tflite",
        engine = DetectionEngine.MEDIAPIPE,
    ),
)

/** Model the camera opens with. */
const val DEFAULT_DETECTION_MODEL_ID = "yolo26n-coco"

/**
 * Resolves [id] to a registry entry, falling back to the default model when
 * the id is unknown — stale remembered state must never crash the camera.
 */
fun detectionModelById(id: String): DetectionModel =
    AVAILABLE_DETECTION_MODELS.firstOrNull { it.id == id }
        ?: AVAILABLE_DETECTION_MODELS.first { it.id == DEFAULT_DETECTION_MODEL_ID }
