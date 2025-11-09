package io.github.rufenkhokhar.face

/**
 * Configuration for platform face detection.
 *
 * The values mirror ML Kit options on Android (`com.google.mlkit.vision.face.FaceDetectorOptions`)
 * and iOS (`MLKFaceDetectorOptions`). Settings affect latency, accuracy, and which fields in
 * the resulting [Face] objects are populated.
 *
 * Notes:
 * - Higher-accuracy modes increase CPU/GPU cost and latency.
 * - Landmarks, contours, and classifications are optional computations; enabling them costs time.
 * - Some features (e.g., ear tips on iOS) may be unavailable regardless of mode.
 *
 * @property performanceMode Speed/accuracy trade-off. Use [PERFORMANCE_MODE_FAST] for real-time
 * preview UIs; use [PERFORMANCE_MODE_ACCURATE] for higher-quality results.
 * @property landMarkMode Whether single-point landmarks are computed.
 * Use [LANDMARK_MODE_NONE] to disable or [LANDMARK_MODE_ALL] to enable.
 * @property contourMode Whether multi-point contours are computed.
 * Use [CONTOUR_MODE_NONE] to disable or [CONTOUR_MODE_ALL] to enable.
 * @property classificationMode Whether expression/eye-open probabilities are computed.
 * Use [CLASSIFICATION_MODE_NONE] to disable or [CLASSIFICATION_MODE_ALL] to enable.
 * When disabled, fields like `smilingProbability` and eye-open probabilities will be `null`.
 * @property minFaceSize Minimum face size as a fraction of the shorter image dimension.
 * Range (0f, 1f]. For example, `0.1f` means faces must be at least 10% of image height/width.
 * Increasing this can reduce false positives and speed up detection on small faces.
 * @property enableTracking Assigns a stable `trackingId` per face across frames when available.
 * Useful for associating results over time; may add overhead.
 */
data class FaceDetectorOptions(
    val performanceMode: Int = PERFORMANCE_MODE_ACCURATE,
    val landMarkMode: Int = LANDMARK_MODE_ALL,
    val contourMode: Int = CONTOUR_MODE_ALL,
    val classificationMode: Int = CLASSIFICATION_MODE_ALL,
    val minFaceSize: Float = .1f,
    val enableTracking: Boolean = true
) {
    companion object {
        /** No single-point landmarks are computed. */
        const val LANDMARK_MODE_NONE = 1
        /** All supported single-point landmarks are computed. */
        const val LANDMARK_MODE_ALL = 2

        /** Optimized for low latency; lower accuracy. */
        const val PERFORMANCE_MODE_FAST = 1
        /** Optimized for accuracy; higher latency. */
        const val PERFORMANCE_MODE_ACCURATE = 2

        /** No classifications (smile/eye open) are computed. */
        const val CLASSIFICATION_MODE_NONE = 1
        /** All supported classifications are computed. */
        const val CLASSIFICATION_MODE_ALL = 2

        /** No contours are computed. */
        const val CONTOUR_MODE_NONE = 1
        /** All supported contours are computed. */
        const val CONTOUR_MODE_ALL = 2
    }
}

