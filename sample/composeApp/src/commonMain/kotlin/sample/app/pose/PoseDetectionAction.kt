package sample.app.pose

import io.github.rufenkhokhar.corevision.PlatformImage
import sample.app.facedetection.FaceDetectionScreenAction

sealed interface PoseDetectionAction {
    data class OnFrameAvailable(val platformImage: PlatformImage) : PoseDetectionAction
}