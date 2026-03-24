package io.github.rufenkhokhar.corevision

import cocoapods.GoogleMLKit.MLKVisionPoint
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
fun MLKVisionPoint.toMLKitPoint(): Point {
    return Point(x = x.toFloat(), y = y.toFloat())
}