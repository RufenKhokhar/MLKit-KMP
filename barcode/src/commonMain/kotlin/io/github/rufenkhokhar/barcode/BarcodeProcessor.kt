@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.barcode

import io.github.rufenkhokhar.corevision.VisionProcessor

/**
 * An interface for processing images to detect and decode barcodes.
 *
 * This processor extends [VisionProcessor] to provide a streamlined way to analyze image frames
 * and extract a list of detected barcodes as [ScanResult] objects. It is designed for
 * multi-platform use, with platform-specific implementations.
 *
 * An instance of this processor can be configured and created using the nested [Builder] class.
 */

expect interface BarcodeProcessor : VisionProcessor<List<ScanResult>> {

    /**
     * A builder class for constructing and configuring [BarcodeProcessor] instances.
     *
     * This class allows for customization of the barcode scanner, such as specifying
     * which barcode formats to detect.
     */
    class Builder() {
        /**
         * Specifies the set of barcode formats the scanner should attempt to recognize.
         *
         * If this method is not called, the scanner will search for all supported formats by default.
         * Calling this method overrides any previously set formats.
         *
         * @param formats A [Set] of [BarcodeFormat] enums to be detected.
         * @return The [Builder] instance for chaining further method calls.
         */
        fun setBarcodeFormats(formats: Set<BarcodeFormat>): Builder

        /**
         * Specifies the barcode formats the scanner should attempt to recognize using a variable number of arguments.
         *
         * This is a convenience method that accepts one or more [BarcodeFormat] enums directly.
         * If this method is not called, the scanner will search for all supported formats by default.
         *
         * @param format A variable number of [BarcodeFormat] arguments that the scanner will search for.
         * @return The [Builder] instance for chaining further method calls.
         */
        fun setBarcodeFormats(vararg format: BarcodeFormat): Builder

        /**
         * Creates a new [BarcodeProcessor] with the current configuration.
         *
         * @return A configured [BarcodeProcessor] instance ready for use.
         */
        fun build(): BarcodeProcessor
    }

}
