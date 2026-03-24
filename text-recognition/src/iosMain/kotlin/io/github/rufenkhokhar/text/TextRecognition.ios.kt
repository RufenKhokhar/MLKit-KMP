@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.text

import io.github.rufenkhokhar.corevision.VisionProcessor

actual interface TextRecognition : VisionProcessor<VisionText> {
    actual companion object {
        actual fun getInstance(): TextRecognition {
            return IOSTextRecognition()
        }
    }
}