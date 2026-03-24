package sample.app.text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.close
import io.github.rufenkhokhar.text.TextRecognition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sample.app.barcode.BarcodeScreenState

class TextRecognitionViewModel : ViewModel() {
    private val textRecognition = TextRecognition.getInstance()
    private var frameProcessing = false
    private val _state = MutableStateFlow(TextRecognitionState())
    val state = _state.asStateFlow()

    fun onAction(action: TextRecognitionAction) {
        when (action) {
            is TextRecognitionAction.OnOnFrameAvailable -> {
                processImageFrame(action.platformImage)

            }
        }
    }

    private fun processImageFrame(platformImage: PlatformImage) {
        if (!frameProcessing) {
            println("processImageFrame -> if condition")
            frameProcessing = true
            viewModelScope.launch {
                val result = withContext(Dispatchers.Default) {
                    textRecognition.processImageFrame(platformImage)
                }
                result.onSuccess { text ->
                    println("processImageFrame -> text: ${text.text}")
                    _state.update {
                        it.copy(
                           text = text
                        )
                    }
                }.onFailure {
                    it.printStackTrace()
                }
            }.invokeOnCompletion {
                frameProcessing = false
                platformImage.close()
            }
        } else {
            println("processImageFrame -> else condition")
            platformImage.close()
        }

    }

}