@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.face

import io.github.rufenkhokhar.corevision.VisionProcessor

/**
 * Cross-platform face detector that plugs into a [VisionProcessor] pipeline and returns
 * a list of domain-level [Face] results for each input frame.
 *
 * Platform mapping:
 * - Android: ML Kit Vision Face Detection (`com.google.mlkit.vision.face.FaceDetector`)
 * - iOS: ML Kit Face Detection (`MLKFaceDetector`)
 *
 * Output:
 * - Returns zero or more [Face] objects per frame.
 * - Coordinate values are in image pixels (top-left origin) unless you normalize them upstream.
 * - Optional fields are nullable:
 *   - In [Face]: angles/probabilities/trackingId/boxes may be null if not computed.
 *   - In [Landmarks]: each point may be null if the detector didn’t produce it (e.g., ear tips on iOS).
 *   - In [Contours]: each polyline list may be null if that contour wasn’t computed.
 * - Order of faces is not guaranteed. Use `trackingId` (when available) to correlate faces across frames.
 *
 * Configuration:
 * - Use [getInstance] to create a detector with [FaceDetectorOptions].
 * - Options control latency, accuracy, and which fields are populated:
 *   - `performanceMode`: FAST for preview, ACCURATE for quality.
 *   - `landMarkMode` / `contourMode`: toggle landmarks and contours.
 *   - `classificationMode`: toggles smiling/eye-open probabilities.
 *   - `minFaceSize`: filters small faces.
 *   - `enableTracking`: enables stable `trackingId` across frames.
 *
 * Threading & lifecycle:
 * - Safe to invoke on a background thread.
 * - Creation and disposal follow your [VisionProcessor] lifecycle.
 * - Propagate errors using the same mechanism as other processors in your pipeline.
 *
 * Example:
 * ```
 * val detector = FaceDetection.getInstance(
 *     FaceDetectorOptions(
 *         performanceMode = FaceDetectorOptions.PERFORMANCE_MODE_FAST,
 *         landMarkMode = FaceDetectorOptions.LANDMARK_MODE_ALL,
 *         contourMode = FaceDetectorOptions.CONTOUR_MODE_ALL,
 *         classificationMode = FaceDetectorOptions.CLASSIFICATION_MODE_ALL,
 *         enableTracking = true
 *     )
 * )
 * // In your pipeline:
 * val faces: List<Face> = detector.processImageFrame(frame)
 * ```
 */
expect interface FaceDetection : VisionProcessor<List<Face>> {

    companion object {
        /**
         * Creates a platform-specific detector with the given configuration.
         *
         * On Android, maps to `com.google.mlkit.vision.face.FaceDetectorOptions`.
         * On iOS, maps to `MLKFaceDetectorOptions`.
         *
         * Fields in the resulting [Face] will be populated based on the chosen modes.
         */
        fun getInstance(options: FaceDetectorOptions = FaceDetectorOptions()): FaceDetection
    }
}

