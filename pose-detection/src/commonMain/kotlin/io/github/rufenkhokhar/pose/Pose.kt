package io.github.rufenkhokhar.pose

data class Pose(val poseLandmarks: List<PoseLandmark>){
    fun getLandmark(landmarkType: Int): PoseLandmark?{
        return poseLandmarks.firstOrNull { it.landmarkType == landmarkType }
    }
}
