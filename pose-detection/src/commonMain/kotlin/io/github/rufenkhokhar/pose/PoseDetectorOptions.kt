package io.github.rufenkhokhar.pose

data class PoseDetectorOptions(
    val detectorMode: Int = SINGLE_IMAGE_MODE
){
    companion object{
        const val STREAM_MODE = 1
        const val SINGLE_IMAGE_MODE = 2

    }
}
