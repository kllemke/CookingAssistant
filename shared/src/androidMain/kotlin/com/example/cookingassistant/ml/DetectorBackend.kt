package com.example.cookingassistant.ml

/** Detector implementations wired into the camera. */
enum class DetectorBackend {
    MEDIAPIPE_EFFICIENTDET_LITE0,
    LITERT_YOLO26N,
}

/**
 * Backend used by the camera screen. Flip this constant to compare
 * implementations without touching the camera pipeline.
 */
val ACTIVE_DETECTOR_BACKEND: DetectorBackend = DetectorBackend.LITERT_YOLO26N
