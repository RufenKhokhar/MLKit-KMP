package sample.app.facedetection

import io.github.rufenkhokhar.corevision.PlatformImage

sealed interface FaceDetectionScreenAction {
    data class OnFrameAvailable(val platformImage: PlatformImage) : FaceDetectionScreenAction
}