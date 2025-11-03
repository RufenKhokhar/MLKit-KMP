@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.barcode

import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import io.github.rufenkhokhar.corevision.VisionProcessor

actual interface BarcodeProcessor : VisionProcessor<List<ScanResult>> {
    actual class Builder {
        private val barcodeScannerOptions = BarcodeScannerOptions.Builder()
        private var nativeFormats: List<Int> = emptyList()
        actual fun setBarcodeFormats(formats: Set<BarcodeFormat>): Builder {
            nativeFormats = formats.map { it.toNativeFormat() }
            barcodeScannerOptions.setBarcodeFormats(
                nativeFormats.first(),
                *nativeFormats.drop(1).toIntArray()
            )
            return this
        }

        actual fun setBarcodeFormats(vararg format: BarcodeFormat): Builder {
            nativeFormats = format.map { it.toNativeFormat() }
            barcodeScannerOptions.setBarcodeFormats(
                nativeFormats.first(),
                *nativeFormats.drop(1).toIntArray()
            )
            return this
        }

        actual fun build(): BarcodeProcessor {
            if (nativeFormats.isEmpty()) {
                setBarcodeFormats(BarcodeFormat.entries.toSet())
            }
            return AndroidBarcodeProcessor(barcodeScannerOptions.build())
        }
    }
}

internal fun BarcodeFormat.toNativeFormat(): Int {
    return when (this) {
        BarcodeFormat.FORMAT_CODE_128 -> Barcode.FORMAT_CODE_128
        BarcodeFormat.FORMAT_CODE_39 -> Barcode.FORMAT_CODE_39
        BarcodeFormat.FORMAT_CODE_93 -> Barcode.FORMAT_CODE_93
        BarcodeFormat.FORMAT_CODABAR -> Barcode.FORMAT_CODABAR
        BarcodeFormat.FORMAT_EAN_13 -> Barcode.FORMAT_EAN_13
        BarcodeFormat.FORMAT_EAN_8 -> Barcode.FORMAT_EAN_8
        BarcodeFormat.FORMAT_ITF -> Barcode.FORMAT_ITF
        BarcodeFormat.FORMAT_UPC_A -> Barcode.FORMAT_UPC_A
        BarcodeFormat.FORMAT_UPC_E -> Barcode.FORMAT_UPC_E
        BarcodeFormat.FORMAT_QR_CODE -> Barcode.FORMAT_QR_CODE
        BarcodeFormat.FORMAT_PDF417 -> Barcode.FORMAT_PDF417
        BarcodeFormat.FORMAT_AZTEC -> Barcode.FORMAT_AZTEC
        BarcodeFormat.FORMAT_DATA_MATRIX -> Barcode.FORMAT_DATA_MATRIX
    }
}