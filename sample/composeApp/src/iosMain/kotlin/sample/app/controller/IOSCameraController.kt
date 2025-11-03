package sample.app.controller

import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.toPlatformImage
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.position
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.CoreMedia.CMSampleBufferRef
import platform.Foundation.NSSelectorFromString
import platform.QuartzCore.CALayer
import platform.UIKit.UIApplication
import platform.UIKit.UIImageOrientation
import platform.UIKit.UIInterfaceOrientation
import platform.UIKit.UIInterfaceOrientationLandscapeLeft
import platform.UIKit.UIInterfaceOrientationLandscapeRight
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIInterfaceOrientationPortraitUpsideDown
import platform.UIKit.UIView
import platform.UIKit.interfaceOrientation
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import platform.darwin.dispatch_set_target_queue
import platform.posix.QOS_CLASS_USER_INITIATED
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


internal class IOSCameraController : CameraController {
    private val session = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer? = null
    private var videoOutput: AVCaptureVideoDataOutput? = null
    private var videoDeviceInput: AVCaptureDeviceInput? = null


    @OptIn(ExperimentalForeignApi::class)
    private val captureQueue = run {
        val q = dispatch_queue_create("camera.capture.queue", null)
        val qos = dispatch_get_global_queue(QOS_CLASS_USER_INITIATED.toLong(), 0u)
        dispatch_set_target_queue(q, qos)
        q
    }
    private var frameListener: FrameListener? = null

    @OptIn(ExperimentalForeignApi::class)
    private val sampleDelegate = SampleBufferDelegate { frameListener?.invoke(it) }

    override fun setFrameListener(listener: FrameListener?) {
        frameListener = listener
    }


    override suspend fun startPreview(target: UIView) {
        ensureVideoPermission()
        configureSession()
        attachPreviewIfNeeded(target)
        session.startRunning()
    }

    override fun stop() {
        if (session.running) session.stopRunning()
        previewLayer?.removeFromSuperlayer()
        previewLayer = null
        videoOutput?.let { out -> if (session.outputs.contains(out)) session.removeOutput(out) }
        videoDeviceInput?.let { inp -> if (session.inputs.contains(inp)) session.removeInput(inp) }
        videoOutput = null
        videoDeviceInput = null
    }

    @OptIn(ExperimentalForeignApi::class)
    fun updateOrientationForCurrentUI(orientation: UIInterfaceOrientation) {
        val av = orientation.toAVOrientation()
        previewLayer?.connection?.let { conn ->
            if (conn.supportsVideoOrientation()) conn.videoOrientation = av
        }
        videoOutput?.connectionWithMediaType(AVMediaTypeVideo)?.let { conn ->
            if (conn.supportsVideoOrientation()) conn.videoOrientation = av
            conn.setVideoMirroringIfSupported(shouldMirrorFront = isFrontCamera())
        }
        previewLayer?.frame = previewLayer?.superlayer()?.bounds ?: CGRectZero.readValue()
    }
    // ————————————————————————————————————————————————————————————————

    @OptIn(ExperimentalForeignApi::class)
    private fun attachPreviewIfNeeded(target: UIView) {
        dispatch_async(dispatch_get_main_queue()) {
            val layer = AVCaptureVideoPreviewLayer(session = session)
            layer.setFrame(target.bounds)
            layer.setVideoGravity(AVLayerVideoGravityResizeAspectFill)
            target.layer.addSublayer(layer as CALayer)
            previewLayer = layer
            val current =
                UIApplication.sharedApplication.keyWindow?.rootViewController?.interfaceOrientation
                    ?: UIInterfaceOrientationPortrait
            updateOrientationForCurrentUI(current)
            resizePreview(to = target.bounds)
        }

    }

    @OptIn(ExperimentalForeignApi::class)
    fun resizePreview(to: CValue<CGRect>) {
        dispatch_async(dispatch_get_main_queue()) {
            previewLayer?.frame = to
        }
    }


    @OptIn(ExperimentalForeignApi::class)
    private fun configureSession() {
        session.beginConfiguration()
        try {
            session.sessionPreset = AVCaptureSessionPresetPhoto
            val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                ?: error("No camera device")
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, error = null)
                ?: error("No camera input")
            if (session.canAddInput(input)) {
                session.addInput(input)
                videoDeviceInput = input
            }
            val out = AVCaptureVideoDataOutput().apply {
                alwaysDiscardsLateVideoFrames = false
                setSampleBufferDelegate(sampleDelegate, captureQueue) // captureQueue = SERIAL
            }
            if (session.canAddOutput(out)) {
                session.addOutput(out)
                videoOutput = out
            }
            videoOutput?.connectionWithMediaType(AVMediaTypeVideo)?.let { conn ->
                if (conn.supportsVideoOrientation()) {
                    conn.videoOrientation = AVCaptureVideoOrientationPortrait
                }
                conn.setVideoMirroringIfSupported(shouldMirrorFront = isFrontCamera())
            }
        } finally {
            session.commitConfiguration()
        }
    }

    private suspend fun ensureVideoPermission() {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> return
            AVAuthorizationStatusNotDetermined -> {
                val granted = suspendCoroutine { cont ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { ok ->
                        cont.resume(ok)
                    }
                }
                if (granted) return
            }
        }
        error("Camera permission not granted")
    }

    private fun isFrontCamera(): Boolean =
        videoDeviceInput?.device?.position == AVCaptureDevicePositionFront
}

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

private fun UIInterfaceOrientation.toAVOrientation(): AVCaptureVideoOrientation =
    when (this) {
        UIInterfaceOrientationPortrait -> AVCaptureVideoOrientationPortrait
        UIInterfaceOrientationPortraitUpsideDown -> AVCaptureVideoOrientationPortraitUpsideDown
        UIInterfaceOrientationLandscapeLeft -> AVCaptureVideoOrientationLandscapeLeft
        UIInterfaceOrientationLandscapeRight -> AVCaptureVideoOrientationLandscapeRight
        else -> AVCaptureVideoOrientationPortrait
    }


private fun AVCaptureVideoOrientation.toUIImageOrientation(): UIImageOrientation =
    when (this) {
        AVCaptureVideoOrientationPortrait -> UIImageOrientation.UIImageOrientationUp
        AVCaptureVideoOrientationPortraitUpsideDown -> UIImageOrientation.UIImageOrientationDown
        AVCaptureVideoOrientationLandscapeLeft -> UIImageOrientation.UIImageOrientationLeft
        AVCaptureVideoOrientationLandscapeRight -> UIImageOrientation.UIImageOrientationRight
        else -> UIImageOrientation.UIImageOrientationUp
    }

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureConnection.supportsVideoOrientation(): Boolean =
    respondsToSelector(NSSelectorFromString("isVideoOrientationSupported")) ||
            respondsToSelector(NSSelectorFromString("videoOrientation"))

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureConnection.safeIsVideoMirrored(): Boolean {
    return when {
        respondsToSelector(NSSelectorFromString("isVideoMirrored")) -> isVideoMirrored()
        respondsToSelector(NSSelectorFromString("videoMirrored")!!) -> videoMirrored
        else -> false
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun AVCaptureConnection.setVideoMirroringIfSupported(shouldMirrorFront: Boolean) {
    if (!respondsToSelector(NSSelectorFromString("isVideoMirroringSupported"))) return
    when {
        respondsToSelector(NSSelectorFromString("setVideoMirrored:")) -> this.setVideoMirrored(
            shouldMirrorFront
        )

        respondsToSelector(NSSelectorFromString("setAutomaticallyAdjustsVideoMirroring:")) -> {
            this.setAutomaticallyAdjustsVideoMirroring(false)
            if (respondsToSelector(NSSelectorFromString("setVideoMirrored:"))) {
                this.setVideoMirrored(shouldMirrorFront)
            }
        }
    }
}
