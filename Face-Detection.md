# Face Detection

Detects faces in images and optionally returns landmarks, contours, head angles, and expression probabilities.

**Artifact:** `mlkit-face-detection` / `mlkit-face-detection-play-services`

## Create the processor

```kotlin
// Default options (accurate mode, landmarks + contours + classification + tracking all enabled)
val detector = FaceDetection.getInstance()

// Custom options
val detector = FaceDetection.getInstance(
    FaceDetectorOptions(
        performanceMode = FaceDetectorOptions.PERFORMANCE_MODE_FAST,
        landMarkMode    = FaceDetectorOptions.LANDMARK_MODE_ALL,
        contourMode     = FaceDetectorOptions.CONTOUR_MODE_ALL,
        classificationMode = FaceDetectorOptions.CLASSIFICATION_MODE_ALL,
        minFaceSize     = 0.1f,
        enableTracking  = true
    )
)
```

### FaceDetectorOptions reference

| Property | Constants | Default | Description |
|---|---|---|---|
| `performanceMode` | `PERFORMANCE_MODE_FAST` / `PERFORMANCE_MODE_ACCURATE` | ACCURATE | Speed vs. accuracy trade-off |
| `landMarkMode` | `LANDMARK_MODE_NONE` / `LANDMARK_MODE_ALL` | ALL | Single-point facial landmarks |
| `contourMode` | `CONTOUR_MODE_NONE` / `CONTOUR_MODE_ALL` | ALL | Multi-point facial outlines |
| `classificationMode` | `CLASSIFICATION_MODE_NONE` / `CLASSIFICATION_MODE_ALL` | ALL | Smile / eye-open probabilities |
| `minFaceSize` | `Float` (0..1) | `0.1f` | Minimum face size as fraction of image |
| `enableTracking` | `Boolean` | `true` | Stable `trackingId` across frames |

## Process frames (common code)

`processImageFrame` is a `suspend` function — call it from a coroutine.

```kotlin
val result = detector.processImageFrame(platformImage)
result.onSuccess { faces ->
    platformImage.close()
    println("Detected ${faces.size} face(s)")

    for (face in faces) {
        println("Bounding box: ${face.boundingBox}")
        println("Head yaw (Y): ${face.headEulerAngleY}")
        println("Smiling: ${face.smilingProbability}")
        println("Tracking ID: ${face.trackingId}")

        face.landmarks?.let { lm ->
            println("Left eye: ${lm.leftEye}")
            println("Nose base: ${lm.noseBase}")
        }
    }
}.onFailure { error ->
    platformImage.close()
    println("Error: $error")
}
```

## Face data model

### `Face`

| Property | Type | Description |
|---|---|---|
| `boundingBox` | `BoundingBox?` | Face rectangle in image pixels |
| `headEulerAngleX` | `Float?` | Pitch — looking up (+) / down (−) |
| `headEulerAngleY` | `Float?` | Yaw — looking right (+) / left (−) |
| `headEulerAngleZ` | `Float?` | Roll — head tilt |
| `leftEyeOpenProbability` | `Float?` | 0–1 probability the left eye is open |
| `rightEyeOpenProbability` | `Float?` | 0–1 probability the right eye is open |
| `smilingProbability` | `Float?` | 0–1 probability the subject is smiling |
| `trackingId` | `Int?` | Stable per-session ID (requires `enableTracking = true`) |
| `landmarks` | `Landmarks?` | Single-point facial landmarks (requires `LANDMARK_MODE_ALL`) |
| `contours` | `Contours?` | Multi-point facial outlines (requires `CONTOUR_MODE_ALL`) |

### `Landmarks` (single points, image pixels)

`mouthLeft`, `mouthRight`, `mouthBottom`, `leftEye`, `rightEye`, `leftCheek`, `rightCheek`, `noseBase`, `leftEar`, `rightEar`, `leftEarTip`*, `rightEarTip`*

> *`leftEarTip` / `rightEarTip` are Android-only; always `null` on iOS.

### `Contours` (polylines, image pixels)

`face`, `leftEyebrowTop`, `leftEyebrowBottom`, `rightEyebrowTop`, `rightEyebrowBottom`, `leftEye`, `rightEye`, `upperLipTop`, `upperLipBottom`, `lowerLipTop`, `lowerLipBottom`, `noseBridge`, `noseBottom`

## Tips

- Use `PERFORMANCE_MODE_FAST` for live camera preview; `PERFORMANCE_MODE_ACCURATE` for photo analysis.
- Disable unused modes (`contourMode`, `classificationMode`) to reduce CPU cost.
- Use `trackingId` to correlate the same face across consecutive frames.
- Always call `platformImage.close()` in both `onSuccess` and `onFailure`.