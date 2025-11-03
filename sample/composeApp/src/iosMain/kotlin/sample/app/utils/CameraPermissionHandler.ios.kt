package sample.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
actual fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {

    LaunchedEffect(Unit) {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> {
                onPermissionGranted()
            }
            AVAuthorizationStatusNotDetermined -> {
                val granted = suspendCoroutine { cont ->
                    AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { ok ->
                        cont.resume(ok)
                    }
                }
                if (granted) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
    }
}