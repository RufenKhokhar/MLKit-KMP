@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.pose

import io.github.rufenkhokhar.corevision.VisionProcessor

expect interface PoseDetection : VisionProcessor<List<Pose>> {
    companion object {
        fun getInstance(options: PoseDetectorOptions = PoseDetectorOptions()): PoseDetection
    }
}