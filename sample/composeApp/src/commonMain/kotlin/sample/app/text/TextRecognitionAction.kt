package sample.app.text

import io.github.rufenkhokhar.corevision.PlatformImage

sealed interface TextRecognitionAction {
    data class OnOnFrameAvailable(val platformImage: PlatformImage) : TextRecognitionAction
}