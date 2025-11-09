package io.github.rufenkhokhar.barcode

import io.github.rufenkhokhar.corevision.BoundingBox
import io.github.rufenkhokhar.corevision.Point

/**
 * Represents the result of a single barcode scan.
 *
 * This data class encapsulates all the information extracted from a detected barcode,
 * including its content, format, and location within the analyzed image.
 *
 * @property displayValue A user-friendly representation of the barcode's content.
 * @property rawValue The raw, unparsed data encoded in the barcode.
 * @property valueType The type of data encoded in the barcode (e.g., TEXT, URL, WIFI).
 * @property boundingBox A rectangular box that encloses the detected barcode in the image.
 * @property cornerPoints The list of corner points of the detected barcode polygon, which can be
 *   useful for more precise location mapping, especially for non-rectangular shapes.
 */
data class ScanResult(
    val displayValue: String?,
    val rawValue: String?,
    val valueType: ValueType,
    val boundingBox: BoundingBox,
    val cornerPoints: List<Point>
)
