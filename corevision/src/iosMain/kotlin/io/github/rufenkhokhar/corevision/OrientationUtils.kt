package io.github.rufenkhokhar.corevision

import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.UIKit.UIImageOrientation

fun AVCaptureVideoOrientation.toUIImageOrientation(): UIImageOrientation =
    when (this) {
        AVCaptureVideoOrientationPortrait -> UIImageOrientation.UIImageOrientationUp
        AVCaptureVideoOrientationPortraitUpsideDown -> UIImageOrientation.UIImageOrientationDown
        AVCaptureVideoOrientationLandscapeLeft -> UIImageOrientation.UIImageOrientationLeft
        AVCaptureVideoOrientationLandscapeRight -> UIImageOrientation.UIImageOrientationRight
        else -> UIImageOrientation.UIImageOrientationUp
    }