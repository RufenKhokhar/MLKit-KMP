package sample.app.barcode

import io.github.rufenkhokhar.corevision.PlatformImage

sealed interface BarcodeScreenAction {
    data class OnOnFrameAvailable(val platformImage: PlatformImage) : BarcodeScreenAction
    data object OnResume : BarcodeScreenAction
    data object OnPause : BarcodeScreenAction
    data object OnResultHide : BarcodeScreenAction
}