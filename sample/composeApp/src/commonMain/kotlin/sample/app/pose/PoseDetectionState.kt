package sample.app.pose

import io.github.rufenkhokhar.pose.Pose

data class PoseDetectionState(
    val pose: List<Pose> = emptyList()
)
