package sample.app.facedetection

import io.github.rufenkhokhar.corevision.PlatformImage
import sample.app.barcode.BarcodeScreenAction

sealed interface FaceDetectionScreenAction {
    data class OnOnFrameAvailable(val platformImage: PlatformImage) : FaceDetectionScreenAction
}