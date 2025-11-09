package io.github.rufenkhokhar.face

import io.github.rufenkhokhar.corevision.BoundingBox
import io.github.rufenkhokhar.corevision.Point
/**
 * Canonical, platform-agnostic face representation for ML-based detection.
 *
 * Coordinates are in the source image’s pixel space unless you normalize them yourself.
 * Angles are in degrees. Probabilities are in [0.0, 1.0].
 *
 * Nullability notes:
 * - Some detectors don’t produce every field. When a value isn’t computed by the SDK,
 *   it will be `null` (for scalars) or `null`/empty (for nested objects/collections).
 *
 * Platform notes:
 * - Android ML Kit usually provides all landmark types listed here.
 * - iOS ML Kit does not expose ear-tip landmarks; expect those to be `null`.
 *
 * @property boundingBox Face rectangle in image coordinates. `null` if unavailable.
 * @property headEulerAngleX Rotation around the X axis (pitch, looking up/down). Degrees.
 * @property headEulerAngleY Rotation around the Y axis (yaw, looking left/right). Degrees.
 * @property headEulerAngleZ Rotation around the Z axis (roll, head tilt). Degrees.
 * @property leftEyeOpenProbability Likelihood the left eye is open (0..1), or `null` if not computed.
 * @property rightEyeOpenProbability Likelihood the right eye is open (0..1), or `null` if not computed.
 * @property smilingProbability Likelihood the subject is smiling (0..1), or `null` if not computed.
 * @property trackingId Stable per-session identifier, if tracking is enabled; `null` otherwise.
 * @property landmarks Single-point facial landmarks. `null` if landmark detection was disabled.
 * @property contours Multi-point facial outlines. `null` if contour detection was disabled.
 */
data class Face(
    val boundingBox: BoundingBox?,
    val headEulerAngleX: Float?,
    val headEulerAngleY: Float?,
    val headEulerAngleZ: Float?,
    val leftEyeOpenProbability: Float?,
    val rightEyeOpenProbability: Float?,
    val smilingProbability: Float?,
    val trackingId: Int?,
    val landmarks: Landmarks?,
    val contours: Contours?
)

/**
 * Single-point facial landmarks in image coordinates (pixels).
 *
 * Any field may be `null` if the detector didn’t return that landmark.
 *
 * Typical uses:
 * - Eye centers for blink detection
 * - Nose base for AR anchor placement
 * - Mouth corners for smile geometry
 *
 * Platform notes:
 * - iOS ML Kit does not provide ear-tip landmarks; `leftEarTip`/`rightEarTip` will be `null`.
 */
data class Landmarks(
    /** Approximate left mouth corner. */
    val mouthLeft: Point?,
    /** Approximate right mouth corner. */
    val mouthRight: Point?,
    /** Approximate bottom center of the mouth. */
    val mouthBottom: Point?,
    /** Pupil/center of the left eye. */
    val leftEye: Point?,
    /** Pupil/center of the right eye. */
    val rightEye: Point?,
    /** Left cheek center. */
    val leftCheek: Point?,
    /** Right cheek center. */
    val rightCheek: Point?,
    /** Nose base (tip/bridge junction depending on SDK). */
    val noseBase: Point?,
    /** Left ear center. */
    val leftEar: Point?,
    /** Right ear center. */
    val rightEar: Point?,
    /** Left ear tip (Android only; `null` on iOS). */
    val leftEarTip: Point?,
    /** Right ear tip (Android only; `null` on iOS). */
    val rightEarTip: Point?
)

/**
 * Multi-point facial contours (ordered polylines) in image coordinates (pixels).
 *
 * Each list contains points in a consistent order as provided by the SDK:
 * - For closed shapes (e.g., eyes, outer face), points typically follow a clockwise/CCW loop.
 * - For lips, “Top” lists trace the upper boundary; “Bottom” lists trace the lower boundary.
 * - For eyebrows and nose, lists trace along the respective ridge/bridge.
 *
 * Lists may be empty if the detector didn’t compute that contour.
 *
 * Common uses:
 * - Precise overlays (e.g., glasses frames, lipstick, masks)
 * - Face mesh approximation for effects
 * - Measuring geometric ratios (eye aspect, lip curvature)
 */
data class Contours(
    /** Outer face oval. Closed loop. */
    val face: List<Point>,
    /** Left eyebrow upper ridge. */
    val leftEyebrowTop: List<Point>,
    /** Left eyebrow lower ridge. */
    val leftEyebrowBottom: List<Point>,
    /** Right eyebrow upper ridge. */
    val rightEyebrowTop: List<Point>,
    /** Right eyebrow lower ridge. */
    val rightEyebrowBottom: List<Point>,
    /** Left eye outline. Closed loop. */
    val leftEye: List<Point>,
    /** Right eye outline. Closed loop. */
    val rightEye: List<Point>,
    /** Upper lip outer boundary (upper edge). */
    val upperLipTop: List<Point>,
    /** Upper lip inner boundary (lower edge of upper lip). */
    val upperLipBottom: List<Point>,
    /** Lower lip inner boundary (upper edge of lower lip). */
    val lowerLipTop: List<Point>,
    /** Lower lip outer boundary (lower edge). */
    val lowerLipBottom: List<Point>,
    /** Nose bridge ridge from between eyes toward the tip. */
    val noseBridge: List<Point>,
    /** Nose base outline around the nostrils. */
    val noseBottom: List<Point>
)

