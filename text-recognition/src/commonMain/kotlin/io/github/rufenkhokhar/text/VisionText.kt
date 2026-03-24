package io.github.rufenkhokhar.text

import io.github.rufenkhokhar.corevision.BoundingBox
import io.github.rufenkhokhar.corevision.Point

data class VisionText(
    val text: String,
    val textBlocks: List<TextBlock>
)

data class TextBlock(
    val text: String,
    val boundingBox: BoundingBox?,
    val cornerPoints: List<Point>,
    val lines: List<Line>
)

data class Line(
    val text: String,
    val boundingBox: BoundingBox?,
    val cornerPoints: List<Point>,
    val elements: List<Element>
)

data class Element(
    val text: String,
    val boundingBox: BoundingBox?,
    val cornerPoints: List<Point>
)
