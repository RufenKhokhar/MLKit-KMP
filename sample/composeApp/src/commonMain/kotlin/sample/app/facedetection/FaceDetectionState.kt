package sample.app.facedetection

import io.github.rufenkhokhar.face.Face

data class FaceDetectionState(
    val faces: List<Face> = emptyList()
)
