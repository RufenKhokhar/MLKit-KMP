package io.github.rufenkhokhar.corevision

import android.graphics.Rect

fun Rect.toBoundingBox(): BoundingBox {
    return BoundingBox(
        left = left.toFloat(),
        top = top.toFloat(),
        right = right.toFloat(),
        bottom = bottom.toFloat()
    )
}