@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.barcode

import cocoapods.GoogleMLKit.MLKBarcodeFormat
import cocoapods.GoogleMLKit.MLKBarcodeFormatAll
import cocoapods.GoogleMLKit.MLKBarcodeFormatAztec
import cocoapods.GoogleMLKit.MLKBarcodeFormatCodaBar
import cocoapods.GoogleMLKit.MLKBarcodeFormatCode128
import cocoapods.GoogleMLKit.MLKBarcodeFormatCode39
import cocoapods.GoogleMLKit.MLKBarcodeFormatCode93
import cocoapods.GoogleMLKit.MLKBarcodeFormatDataMatrix
import cocoapods.GoogleMLKit.MLKBarcodeFormatEAN13
import cocoapods.GoogleMLKit.MLKBarcodeFormatEAN8
import cocoapods.GoogleMLKit.MLKBarcodeFormatITF
import cocoapods.GoogleMLKit.MLKBarcodeFormatPDF417
import cocoapods.GoogleMLKit.MLKBarcodeFormatQRCode
import cocoapods.GoogleMLKit.MLKBarcodeFormatUPCA
import cocoapods.GoogleMLKit.MLKBarcodeFormatUPCE
import cocoapods.GoogleMLKit.MLKBarcodeScannerOptions
import io.github.rufenkhokhar.corevision.VisionProcessor
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
actual interface BarcodeProcessor : VisionProcessor<List<ScanResult>> {
    actual class Builder {
        private val nativeFormats = mutableSetOf<MLKBarcodeFormat>()

        actual fun setBarcodeFormats(formats: Set<BarcodeFormat>): Builder {
            val nativeFormats = formats.map { it.toNativeFormat() }
            this.nativeFormats.addAll(nativeFormats)
            return this
        }

        actual fun setBarcodeFormats(vararg format: BarcodeFormat): Builder {
            val nativeFormats = format.map { it.toNativeFormat() }
            this.nativeFormats.addAll(nativeFormats)
            return this
        }


        actual fun build(): BarcodeProcessor {
            val options = if (nativeFormats.isEmpty()) {
                MLKBarcodeScannerOptions(MLKBarcodeFormatAll)
            } else {
                MLKBarcodeScannerOptions()
            }
            return IOSBarcodeProcessor(options)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun BarcodeFormat.toNativeFormat(): MLKBarcodeFormat {
    return when (this) {
        BarcodeFormat.FORMAT_CODE_128 -> MLKBarcodeFormatCode128
        BarcodeFormat.FORMAT_CODE_39 -> MLKBarcodeFormatCode39
        BarcodeFormat.FORMAT_CODE_93 -> MLKBarcodeFormatCode93
        BarcodeFormat.FORMAT_CODABAR -> MLKBarcodeFormatCodaBar
        BarcodeFormat.FORMAT_EAN_13 -> MLKBarcodeFormatEAN13
        BarcodeFormat.FORMAT_EAN_8 -> MLKBarcodeFormatEAN8
        BarcodeFormat.FORMAT_ITF -> MLKBarcodeFormatITF
        BarcodeFormat.FORMAT_UPC_A -> MLKBarcodeFormatUPCA
        BarcodeFormat.FORMAT_UPC_E -> MLKBarcodeFormatUPCE
        BarcodeFormat.FORMAT_QR_CODE -> MLKBarcodeFormatQRCode
        BarcodeFormat.FORMAT_PDF417 -> MLKBarcodeFormatPDF417
        BarcodeFormat.FORMAT_AZTEC -> MLKBarcodeFormatAztec
        BarcodeFormat.FORMAT_DATA_MATRIX -> MLKBarcodeFormatDataMatrix
    }
}