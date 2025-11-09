@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.face

import io.github.rufenkhokhar.corevision.VisionProcessor


actual interface FaceDetection : VisionProcessor<List<Face>> {
    actual companion object {
        actual fun getInstance(options: FaceDetectorOptions): FaceDetection {
            return AndroidFaceDetection(options)
        }
    }

}