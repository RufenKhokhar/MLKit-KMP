package sample.app.barcode

import io.github.rufenkhokhar.barcode.ScanResult

data class BarcodeScreenState(
    val barcodes: List<ScanResult> = emptyList(),
    val permissionGranted: Boolean = false,
    val showResult: Boolean = false
)
