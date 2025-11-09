package sample.app.facedetection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.close
import io.github.rufenkhokhar.face.FaceDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FaceDetectionViewModel : ViewModel() {
    private val faceDetection = FaceDetection.getInstance()
    private var frameProcessing = false
    private val _state = MutableStateFlow(FaceDetectionState())
    val state = _state.asStateFlow()


    fun onAction(action: FaceDetectionScreenAction){
        when(action){
            is FaceDetectionScreenAction.OnOnFrameAvailable -> {
                processImageFrame(action.platformImage)

            }
        }
    }

    private fun processImageFrame(platformImage: PlatformImage) {
        if (!frameProcessing) {
            frameProcessing = true
            viewModelScope.launch {
                val result = withContext(Dispatchers.Default) {
                    faceDetection.processImageFrame(platformImage)
                }
                result.onSuccess { faces ->
                    println("processImageFrame -> faces: $faces")
                }.onFailure {
                    it.printStackTrace()
                }
            }.invokeOnCompletion {
                frameProcessing = false
                platformImage.close()
            }
        } else {
            platformImage.close()
        }

    }

}