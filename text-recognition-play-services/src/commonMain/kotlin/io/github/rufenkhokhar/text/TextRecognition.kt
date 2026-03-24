@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.text

import io.github.rufenkhokhar.corevision.VisionProcessor

expect interface TextRecognition : VisionProcessor<VisionText> {

    companion object {
        fun getInstance(): TextRecognition
    }
}