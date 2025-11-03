package sample.app

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.toPlatformImage
import java.util.concurrent.Executors


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