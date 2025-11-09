# Examples

## Common code (shared)

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

## Android

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

## iOS (Kotlin/Native)

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
