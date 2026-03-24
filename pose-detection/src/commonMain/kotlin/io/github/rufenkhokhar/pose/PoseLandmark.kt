package io.github.rufenkhokhar.pose

import io.github.rufenkhokhar.corevision.Point
import io.github.rufenkhokhar.corevision.Point3D

data class PoseLandmark(
    val inFrameLikelihood: Float,
    val landmarkType: Int,
    val position: Point,
    val position3D: Point3D
){
    companion object {

        const val NOSE: Int = 0

        const val LEFT_EYE_INNER: Int = 1

        const val LEFT_EYE: Int = 2

        const val LEFT_EYE_OUTER: Int = 3

        const val RIGHT_EYE_INNER: Int = 4

        const val RIGHT_EYE: Int = 5

        const val RIGHT_EYE_OUTER: Int = 6

        const val LEFT_EAR: Int = 7

        const val RIGHT_EAR: Int = 8

        const val LEFT_MOUTH: Int = 9

        const val RIGHT_MOUTH: Int = 10

        const val LEFT_SHOULDER: Int = 11

        const val RIGHT_SHOULDER: Int = 12

        const val LEFT_ELBOW: Int = 13

        const val RIGHT_ELBOW: Int = 14

        const val LEFT_WRIST: Int = 15

        const val RIGHT_WRIST: Int = 16

        const val LEFT_PINKY: Int = 17

        const val RIGHT_PINKY: Int = 18

        const val LEFT_INDEX: Int = 19

        const val RIGHT_INDEX: Int = 20

        const val LEFT_THUMB: Int = 21

        const val RIGHT_THUMB: Int = 22

        const val LEFT_HIP: Int = 23

        const val RIGHT_HIP: Int = 24

        const val LEFT_KNEE: Int = 25

        const val RIGHT_KNEE: Int = 26

        const val LEFT_ANKLE: Int = 27

        const val RIGHT_ANKLE: Int = 28

        const val LEFT_HEEL: Int = 29

        const val RIGHT_HEEL: Int = 30

        const val LEFT_FOOT_INDEX: Int = 31

        const val RIGHT_FOOT_INDEX: Int = 32
    }
}
