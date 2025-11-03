package sample.app.barcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.rufenkhokhar.barcode.BarcodeProcessor
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.close
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BarcodeScanViewModel : ViewModel() {
    private val barcodeProcesser = BarcodeProcessor.Builder().build()
    private var scanningActive = true
    private var frameProcessing = false
    private val _state = MutableStateFlow(BarcodeScreenState())
    val state = _state.asStateFlow()


    fun onAction(action: BarcodeScreenAction) {
        when (action) {
            is BarcodeScreenAction.OnOnFrameAvailable -> {
                processImageFrame(action.platformImage)

            }

            BarcodeScreenAction.OnPause -> {
                scanningActive = false
            }

            BarcodeScreenAction.OnResume -> {
                scanningActive = true
            }

            BarcodeScreenAction.OnResultHide -> {
                scanningActive = true
                _state.update { it.copy(showResult = false) }
            }
        }
    }

    private fun processImageFrame(platformImage: PlatformImage) {
        if (!frameProcessing && scanningActive) {
            println("processImageFrame -> if condition")
            frameProcessing = true
            viewModelScope.launch {
                val result = withContext(Dispatchers.Default) {
                    barcodeProcesser.processImageFrame(platformImage)
                }
                result.onSuccess { barcodes ->
                    println("processImageFrame -> barcodes: $barcodes")
                    scanningActive = barcodes.isEmpty()
                    _state.update {
                        it.copy(barcodes = barcodes, showResult = barcodes.isNotEmpty())
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