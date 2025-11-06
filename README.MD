# MLKit-KMP

Kotlin Multiplatform bindings for Google ML Kit. This library wraps ML Kit’s native SDKs behind a clean, shared Kotlin API, so you can integrate all features like Vision and NLP in one place and run them on Android and iOS. It handles platform interop, image inputs, threading, and results mapping for you, with Swift-friendly symbols on iOS and straightforward Gradle setup on Android.

---

![Maven Central](https://img.shields.io/maven-central/v/io.github.rufenkhokhar/mlkit-barcode)
![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green.svg)
![Platform](https://img.shields.io/badge/Platforms-Android%20|%20iOS-blue)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)
![Issues](https://img.shields.io/github/issues/rufenkhokhar/MLKit-KMP)
![Stars](https://img.shields.io/github/stars/rufenkhokhar/MLKit-KMP?style=social)

---
## Requirements

| Target  | Minimum OS | Architectures                                 |
| ------- | ---------- | --------------------------------------------- |
| Android | API 23+    | All common Android ABIs                       |
| iOS     | 15.0+      | Device + Simulator (arm64, x64 as applicable) |

---

## Roadmap (ML Kit API coverage)

| Category | API | Support |
|-----------|-----|----------|
| **Vision** | Text Recognition v2 | ⏳ Planned |
|  | Face Detection | ⏳ Planned |
|  | Face Mesh Detection  | ⏳ Planned |
|  | Pose Detection  | ⏳ Planned |
|  | Selfie Segmentation  | ⏳ Planned |
|  | Subject Segmentation  | ⏳ Planned |
|  | Document Scanner | ⏳ Planned |
|  | Barcode Scanning | ✅ Available |
|  | Image Labeling | ⏳ Planned |
|  | Object Detection & Tracking | ⏳ Planned |
|  | Digital Ink Recognition | ⏳ Planned |
| **Natural Language** | Language Identification | ⏳ Planned |
|  | Translation | ⏳ Planned |
|  | Smart Reply | ⏳ Planned |
|  | Entity Extraction  | ⏳ Planned |

---
## Getting started (Barcode Scanning)

### 1) Add the dependency (KMP project)

In your shared KMP module’s `build.gradle.kts`:

```kotlin
kotlin {
    // … your targets …
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.rufenkhokhar:mlkit-barcode:1.0.0")
            }
        }
    }
}
```

Make sure `mavenCentral()` is present in your repositories:

```kotlin
repositories {
    mavenCentral()
}
```


### 2) iOS host app: add Google ML Kit via CocoaPods

The iOS application that embeds your KMP framework must include the native Google ML Kit pods. Create a `Podfile` in the iOS app and add:

```ruby
platform :ios, '15.0'
workspace 'iosApp'
use_frameworks! :linkage => :static

target 'iosApp' do
  pod 'GoogleMLKit/BarcodeScanning'
end
```

Then run:

```bash
pod install
open iosApp.xcworkspace
```
> **Why this step?** Google distributes ML Kit’s native binaries for iOS through CocoaPods. The Kotlin Multiplatform wrapper depends on these binaries, so you need to install them using `pod install`. After installation, always open your iOS project using the generated `.xcworkspace` file instead of the `.xcodeproj` and run the ios app via Xcode.


### Common code (shared)

Create the processor with the builder and optionally restrict formats for better performance:

```kotlin
val processor = BarcodeProcessor.Builder()
    // Option A: pass a Set
    .setBarcodeFormats(setOf(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_13))
    // Option B: vararg convenience
    //.setBarcodeFormats(BarcodeFormat.QR_CODE, BarcodeFormat.EAN_13)
    .build()
```
- **Formats:** Restricting formats reduces work per frame and speeds up detection.
- **Lifecycle:** Reuse a single `BarcodeProcessor` instance rather than creating one per frame.
---

## Image input helpers

Prepare platform images using the provided extension helpers.

### Android

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


### iOS (Kotlin/Native)

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

---


## Example

### Common code (shared)

```kotlin

@Composable
fun App() {
    Box(modifier = Modifier.fillMaxSize()) {
        val barcodeProcessor = remember { BarcodeProcessor.Builder().build() }
        var frameAnalyzing by remember { mutableStateOf(false) }
        val coroutine = rememberCoroutineScope()
        CameraLayout(modifier = Modifier.fillMaxSize()) { platformImage ->
            if (frameAnalyzing.not()) {
                frameAnalyzing = true
                coroutine.launch {
                    val result = barcodeProcessor.processImageFrame(platformImage)
                    result.onSuccess {
                        platformImage.close()
                        println("result: $it")
                    }.onFailure {
                        platformImage.close()
                        println("error: $it")
                    }
                }.invokeOnCompletion {
                    frameAnalyzing = false
                }
            } else {
                platformImage.close()
            }
        }
    }
}
@Composable
expect fun CameraLayout(
    modifier: Modifier = Modifier,
    onFrameAvailable: (PlatformImage) -> Unit
)
```

### Android

```kotlin

@Composable
actual fun CameraLayout(
    modifier: Modifier,
    onFrameAvailable: (PlatformImage) -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val imageAnalysis = remember { ImageAnalysis.Builder().build() }
    val imageAnalyzer = remember { ImageAnalysis.Analyzer { onFrameAvailable(it.toPlatformImage()) } }
    val backgroundExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(factory = { previewView }, modifier = modifier)
    LifecycleStartEffect(Unit) {
        cameraProviderFuture.addListener(
            {
                val preview = Preview.Builder().build()
                preview.surfaceProvider = previewView.surfaceProvider
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                imageAnalysis.setAnalyzer(backgroundExecutor, imageAnalyzer)
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )

            },
            ContextCompat.getMainExecutor(context)
        )
        onStopOrDispose {
            imageAnalysis.clearAnalyzer()
        }
    }
}
```

### iOS (Kotlin/Native)

```kotlin


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraLayout(
    modifier: Modifier,
    onFrameAvailable: (PlatformImage) -> Unit
) {
    val cameraController = remember { IOSCameraController() }
    val uiView = remember { UIView() }

    UIKitView(
        modifier = modifier,
        factory = { uiView },
        update = {
            cameraController.resizePreview(it.bounds)
        }
    )
    LaunchedEffect(Unit) {
        cameraController.startPreview(uiView)
        cameraController.setFrameListener(onFrameAvailable)
    }
    DisposableEffect(Unit) {
        onDispose {
            cameraController.stop()
        }
    }

}

// In IOSCameraController how i process the frames
// for more details see-> sample/composeApp/src/iosMain/kotlin/sample/app/controller/IOSCameraController.kt

@OptIn(ExperimentalForeignApi::class)
private class SampleBufferDelegate(
    private val onFrame: (PlatformImage) -> Unit
) : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {


    @OptIn(BetaInteropApi::class)
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputSampleBuffer: CMSampleBufferRef?,
        fromConnection: AVCaptureConnection
    ) {
        didOutputSampleBuffer?.let {
            onFrame(it.toPlatformImage(fromConnection.videoOrientation))
        }
    }
}
```
---
## Contributing

PRs and issues are welcome. Please file reproducible samples when reporting bugs.

---

## License

MIT © 2025 Rufen Khokhar


