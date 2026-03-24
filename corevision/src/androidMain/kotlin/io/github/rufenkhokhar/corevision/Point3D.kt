package io.github.rufenkhokhar.corevision

import com.google.mlkit.vision.common.PointF3D

fun PointF3D.toMLKitPoint3D(): Point3D {
    return Point3D(x = x, y = y, z = z)
}