package io.github.rufenkhokhar.corevision

import cocoapods.GoogleMLKit.MLKVision3DPoint
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun MLKVision3DPoint.toMLKitPoint3D(): Point3D {
    return Point3D(x = x().toFloat(), y = y().toFloat(), z = z().toFloat())
}
@OptIn(ExperimentalForeignApi::class)
fun MLKVision3DPoint.toMLKitPoint(): Point {
    return Point(x = x().toFloat(), y = y().toFloat())
}
