package sample.app.controller



import io.github.rufenkhokhar.corevision.PlatformImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
typealias FrameListener = (PlatformImage) -> Unit


internal interface CameraController {
    suspend fun startPreview(target: UIView)
    fun stop()
    fun setFrameListener(listener: FrameListener?)
}