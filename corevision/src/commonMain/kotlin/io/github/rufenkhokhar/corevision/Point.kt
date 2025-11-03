package io.github.rufenkhokhar.corevision

/**
 * Represents a two-dimensional point in an image or on a plane.
 *
 * This data class is used to define a specific location using integer coordinates.
 * It is often used to specify the corners of a bounding box or other feature points
 * within an image.
 *
 * @property x The horizontal coordinate of the point.
 * @property y The vertical coordinate of the point.
 */
data class Point(
    val x: Int,
    val y: Int
)
