package io.github.rufenkhokhar.barcode

/**
 * Enumerates the different barcode formats that can be recognized and decoded.
 */
enum class BarcodeFormat {
    /**
     * A high-density linear symbology capable of encoding all 128 ASCII characters.
     * It is widely used in logistics and transportation for labeling and tracking.
     */
    FORMAT_CODE_128,

    /**
     * A variable-length, discrete barcode symbology that can encode uppercase letters (A-Z),
     * digits (0-9), and a few special characters. It is often used for inventory management
     * in various industries.
     */
    FORMAT_CODE_39,

    /**
     * An alphanumeric, variable-length symbology that improves upon Code 39 by offering
     * higher data density. It is used by Canada Post to encode supplementary delivery information.
     */
    FORMAT_CODE_93,

    /**
     * A self-checking linear barcode symbology that can encode numbers and four special characters (A, B, C, D).
     * It is commonly used in logistics, blood banks, and libraries.
     */
    FORMAT_CODABAR,

    /**
     * The European Article Number, a 13-digit barcode standard used globally for marking retail goods.
     * It is a superset of the 12-digit UPC-A standard.
     */
    FORMAT_EAN_13,

    /**
     * A compressed, 8-digit version of the EAN-13 barcode designed for smaller products where
     * a full-sized EAN-13 barcode would not fit.
     */
    FORMAT_EAN_8,

    /**
     * Interleaved 2 of 5 is a continuous, numeric-only barcode symbology that encodes pairs of digits.
     * It is primarily used for distribution and warehouse applications, often printed on corrugated cardboard.
     */
    FORMAT_ITF,

    /**
     * The Universal Product Code, a 12-digit barcode used extensively in the United States, Canada,
     * and other countries for tracking trade items in stores.
     */
    FORMAT_UPC_A,

    /**
     * A zero-suppressed variation of UPC-A, allowing for a more compact, 6-digit barcode for use on
     * small packages and products.
     */
    FORMAT_UPC_E,

    /**
     * A two-dimensional matrix barcode that can store a large amount of data, including URLs, text,
     * and contact information. It is known for its fast readability and high fault tolerance.
     */
    FORMAT_QR_CODE,

    /**
     * A stacked linear barcode format used for applications that require storing a large amount of data,
     * such as driver's licenses, transport, and government identification.
     */
    FORMAT_PDF417,

    /**
     * A two-dimensional matrix barcode with a distinct bull's-eye pattern at its center, allowing it to
     * be read even if it's distorted. It is space-efficient and used in ticketing and transportation.
     */
    FORMAT_AZTEC,

    /**
     * A two-dimensional barcode consisting of black and white cells arranged in a square or rectangular pattern.
     * It is known for its high data density and is often used for marking small electronic components and for logistics.
     */
    FORMAT_DATA_MATRIX
}
