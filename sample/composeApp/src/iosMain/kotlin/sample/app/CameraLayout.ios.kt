package sample.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.github.rufenkhokhar.corevision.PlatformImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView
import sample.app.controller.IOSCameraController

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