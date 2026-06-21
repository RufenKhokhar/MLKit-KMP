# Pose Detection

Detects human body poses in images, returning 33 landmarks covering the full body skeleton.

**Artifact:** `mlkit-pose-detection`

## Create the processor

```kotlin
// Default — single image mode
val detector = PoseDetection.getInstance()

// Stream mode (optimized for consecutive frames from a camera)
val detector = PoseDetection.getInstance(
    PoseDetectorOptions(detectorMode = PoseDetectorOptions.STREAM_MODE)
)
```

### PoseDetectorOptions reference

| Property | Constants | Default | Description |
|---|---|---|---|
| `detectorMode` | `SINGLE_IMAGE_MODE` / `STREAM_MODE` | `SINGLE_IMAGE_MODE` | Use `STREAM_MODE` for live camera; `SINGLE_IMAGE_MODE` for static images |

## Process frames (common code)

`processImageFrame` is a `suspend` function — call it from a coroutine.

```kotlin
val result = detector.processImageFrame(platformImage)
result.onSuccess { poses ->
    platformImage.close()
    println("Detected ${poses.size} pose(s)")

    for (pose in poses) {
        val leftShoulder = pose.getLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getLandmark(PoseLandmark.RIGHT_SHOULDER)

        leftShoulder?.let {
            println("Left shoulder: ${it.position}, likelihood: ${it.inFrameLikelihood}")
        }
    }
}.onFailure { error ->
    platformImage.close()
    println("Error: $error")
}
```

## Data model

### `Pose`

| Property / Method | Description |
|---|---|
| `poseLandmarks: List<PoseLandmark>` | All detected landmarks |
| `getLandmark(type: Int): PoseLandmark?` | Convenience lookup by landmark type constant |

### `PoseLandmark`

| Property | Type | Description |
|---|---|---|
| `landmarkType` | `Int` | One of the `PoseLandmark.*` constants |
| `position` | `Point` | 2D position in image pixels |
| `position3D` | `Point3D` | 3D position (x, y in pixels; z relative depth) |
| `inFrameLikelihood` | `Float` | 0–1 confidence the landmark is within the frame |

### Landmark constants

| Constant | Body part |
|---|---|
| `NOSE` | Nose tip |
| `LEFT_EYE_INNER` / `LEFT_EYE` / `LEFT_EYE_OUTER` | Left eye points |
| `RIGHT_EYE_INNER` / `RIGHT_EYE` / `RIGHT_EYE_OUTER` | Right eye points |
| `LEFT_EAR` / `RIGHT_EAR` | Ears |
| `LEFT_MOUTH` / `RIGHT_MOUTH` | Mouth corners |
| `LEFT_SHOULDER` / `RIGHT_SHOULDER` | Shoulders |
| `LEFT_ELBOW` / `RIGHT_ELBOW` | Elbows |
| `LEFT_WRIST` / `RIGHT_WRIST` | Wrists |
| `LEFT_PINKY` / `RIGHT_PINKY` | Pinky fingers |
| `LEFT_INDEX` / `RIGHT_INDEX` | Index fingers |
| `LEFT_THUMB` / `RIGHT_THUMB` | Thumbs |
| `LEFT_HIP` / `RIGHT_HIP` | Hips |
| `LEFT_KNEE` / `RIGHT_KNEE` | Knees |
| `LEFT_ANKLE` / `RIGHT_ANKLE` | Ankles |
| `LEFT_HEEL` / `RIGHT_HEEL` | Heels |
| `LEFT_FOOT_INDEX` / `RIGHT_FOOT_INDEX` | Foot index toes |

## Tips

- Use `STREAM_MODE` for live camera feed — it reuses state from the previous frame for lower latency.
- Use `SINGLE_IMAGE_MODE` for processing individual photos.
- Filter by `inFrameLikelihood > 0.5f` before using a landmark's coordinates.
- Always call `platformImage.close()` in both `onSuccess` and `onFailure`.