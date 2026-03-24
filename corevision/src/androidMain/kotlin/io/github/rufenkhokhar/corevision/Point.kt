package io.github.rufenkhokhar.corevision

import android.graphics.Point
import android.graphics.PointF

fun Point.toMLKitPoint(): io.github.rufenkhokhar.corevision.Point {
    return Point(x = x.toFloat(), y = y.toFloat())
}
fun PointF.toMLKitPoint(): io.github.rufenkhokhar.corevision.Point {
    return Point(x = x, y = y)
}


fun Array<Point>.toMlKitPoints(): List<io.github.rufenkhokhar.corevision.Point> {
    return map { it.toMLKitPoint() }
}