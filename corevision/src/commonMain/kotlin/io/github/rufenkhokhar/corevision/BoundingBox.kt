package io.github.rufenkhokhar.corevision

/**
 * Represents a rectangular region in an image, defined by four float coordinates.
 *
 * This data class is used to specify the location and size of an object detected within an image.
 * The coordinates define the edges of the rectangle.
 *
 * @property left The x-coordinate of the left edge of the bounding box.
 * @property top The y-coordinate of the top edge of the bounding box.
 * @property right The x-coordinate of the right edge of the bounding box.
 * @property bottom The y-coordinate of the bottom edge of the bounding box.
 */
data class BoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

