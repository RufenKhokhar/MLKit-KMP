# Text Recognition

Detects and extracts text from images. Returns a structured hierarchy of blocks, lines, and elements.

**Artifact:** `mlkit-text-recognition` / `mlkit-text-recognition-play-services`

## Create the processor

```kotlin
val processor = TextRecognition.getInstance()
```

## Process frames (common code)

`processImageFrame` is a `suspend` function — call it from a coroutine.

```kotlin
val result = processor.processImageFrame(platformImage)
result.onSuccess { visionText ->
    platformImage.close()
    println("Full text: ${visionText.text}")

    for (block in visionText.textBlocks) {
        println("Block: ${block.text}")
        for (line in block.lines) {
            println("  Line: ${line.text}")
            for (element in line.elements) {
                println("    Element: ${element.text}")
            }
        }
    }
}.onFailure { error ->
    platformImage.close()
    println("Error: $error")
}
```

## Result types

| Type | Description |
|---|---|
| `VisionText` | Top-level result; `text` contains the full detected string |
| `TextBlock` | A logical paragraph-level group of text |
| `Line` | A single line within a block |
| `Element` | A single word or symbol within a line |

Each of `TextBlock`, `Line`, and `Element` also exposes:
- `boundingBox: BoundingBox?` — the bounding rectangle in image pixels
- `cornerPoints: List<Point>` — the four corner points of the rotated bounding box

## Tips

- **Reuse the processor** — `TextRecognition.getInstance()` is lightweight but there's no need to recreate it per frame.
- **Close the image** — always call `platformImage.close()` in both `onSuccess` and `onFailure`.
- **Performance** — Text recognition is heavier than barcode scanning; throttle frames in a live preview to avoid queuing.