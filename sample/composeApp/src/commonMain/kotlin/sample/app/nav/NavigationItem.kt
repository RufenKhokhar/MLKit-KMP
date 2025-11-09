package sample.app.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationItem {

    @Serializable
    data object Home: NavigationItem
    @Serializable
    data object Barcode: NavigationItem
    @Serializable
    data object FaceDetection: NavigationItem
}