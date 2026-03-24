@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.pose

import io.github.rufenkhokhar.corevision.VisionProcessor

actual interface PoseDetection :
    VisionProcessor<List<Pose>> {
    actual companion object {
        actual fun getInstance(options: PoseDetectorOptions): PoseDetection {
           return IOSPoseDetection(options)
        }
    }
}