package sample.app.pose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.close
import io.github.rufenkhokhar.pose.PoseDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sample.app.facedetection.FaceDetectionScreenAction
import sample.app.facedetection.FaceDetectionState

class PoseDetectionViewModel: ViewModel() {

    private val poseDetection = PoseDetection.getInstance()
    private var frameProcessing = false
    private val _state = MutableStateFlow(PoseDetectionState())
    val state = _state.asStateFlow()


    fun onAction(action: PoseDetectionAction){
        when(action){
            is PoseDetectionAction.OnFrameAvailable -> {
                processImageFrame(action.platformImage)

            }
        }
    }

    private fun processImageFrame(platformImage: PlatformImage) {
        if (!frameProcessing) {
            frameProcessing = true
            viewModelScope.launch {
                val result = withContext(Dispatchers.Default) {
                    poseDetection.processImageFrame(platformImage)
                }
                result.onSuccess { pose ->
                    _state.update { it.copy(pose = pose)}
                }.onFailure { throwable ->
                    _state.update { it.copy(pose = emptyList())}
                    throwable.printStackTrace()
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