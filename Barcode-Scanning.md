# Barcode Scanning

## Create the processor

```kotlin
val processor = BarcodeProcessor.Builder()
    // Option A: pass a Set
    .setBarcodeFormats(setOf(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_13))
    // Option B: vararg convenience
    //.setBarcodeFormats(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_13)
    .build()
```

### Tips
- **Formats:** Restricting formats reduces work per frame and speeds up detection.
- **Lifecycle:** Reuse a single `BarcodeProcessor` instance rather than creating one per frame.

## Process frames (common code)

```kotlin
val result = barcodeProcessor.processImageFrame(platformImage)
result.onSuccess {
    platformImage.close()
    println("result: $it")
}.onFailure {
    platformImage.close()
    println("error: $it")
}
```
