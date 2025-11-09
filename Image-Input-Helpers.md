# Image Input Helpers

Prepare platform images using the provided extension helpers.

## Android

```kotlin
// ImageProxy → PlatformImage
fun ImageProxy.toPlatformImage(): PlatformImage

// Bitmap → PlatformImage (optionally specify rotation)
fun Bitmap.toPlatformImage(rotationDegrees: Int = 0): PlatformImage

// ByteArray (encoded bitmap) → PlatformImage
fun ByteArray.toPlatformImage(rotationDegrees: Int = 0): PlatformImage

// Uri (file/content) → PlatformImage
fun Uri.toPlatformImage(context: Context): PlatformImage 
```

**Notes**
- `ImageProxy` is the most efficient path for CameraX frames.
- If you pass a `Bitmap` or `ByteArray`, supply a `rotationDegrees` if your source isn’t upright.
- `Uri` supports `content://` and `file://` provided the app has permission.

## iOS (Kotlin/Native)

```kotlin
// CMSampleBufferRef → PlatformImage (using UIImageOrientation)
fun CMSampleBufferRef.toPlatformImage(orientation: UIImageOrientation): PlatformImage 

// NSURL (file) → PlatformImage via UIImage
fun NSURL.toPlatformImage(): PlatformImage 

// NSURL (any) → PlatformImage via NSData
fun NSURL.toPlatformImageViaData(): PlatformImage 

// NSData → PlatformImage
fun NSData.toPlatformImage(): PlatformImage 

// UIImage → PlatformImage 
fun UIImage.toPlatformImage(): PlatformImage 

// CMSampleBufferRef → PlatformImage (using AVCaptureVideoOrientation)
fun CMSampleBufferRef.toPlatformImage(orientation: AVCaptureVideoOrientation): PlatformImage 
```

**Notes**
- Prefer `CMSampleBufferRef` for real‑time camera frames.
- Orientation is critical on iOS. Use the overload that accepts `UIImageOrientation` or map from `AVCaptureVideoOrientation`.
- `NSURL` helpers make it easy to process still images from file paths or data.
