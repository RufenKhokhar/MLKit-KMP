@file:OptIn(ExperimentalForeignApi::class)
@file:Suppress("CAST_NEVER_SUCCEEDS")

package io.github.rufenkhokhar.pose


import cocoapods.GoogleMLKit.MLKPoseDetectorOptions
import cocoapods.MLKitPoseDetectionCommon.MLKCommonPoseDetectorOptions
import cocoapods.GoogleMLKit.MLKVisionImage
import cocoapods.MLKitPoseDetectionCommon.MLKPose
import cocoapods.MLKitPoseDetectionCommon.MLKPoseDetector
import cocoapods.MLKitPoseDetectionCommon.MLKPoseDetectorModeSingleImage
import cocoapods.MLKitPoseDetectionCommon.MLKPoseDetectorModeStream
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmark
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkType
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftAnkle
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftEar
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftElbow
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftEye
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftEyeInner
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftEyeOuter
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftHeel
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftHip
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftIndexFinger
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftKnee
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftPinkyFinger
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftShoulder
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftThumb
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftToe
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeLeftWrist
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeMouthLeft
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeMouthRight
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeNose
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightAnkle
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightEar
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightElbow
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightEye
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightEyeInner
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightEyeOuter
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightHeel
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightHip
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightIndexFinger
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightKnee
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightPinkyFinger
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightShoulder
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightThumb
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightToe
import cocoapods.MLKitPoseDetectionCommon.MLKPoseLandmarkTypeRightWrist
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import io.github.rufenkhokhar.corevision.Point
import io.github.rufenkhokhar.corevision.Point3D
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import objcnames.classes.MLKVision3DPoint
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class IOSPoseDetection(options: PoseDetectorOptions) : PoseDetection {
    private val poseDetection = MLKPoseDetector.poseDetectorWithOptions(buildOptions(options) as MLKCommonPoseDetectorOptions)

    override suspend fun processImageFrame(image: PlatformImage): Result<List<Pose>> {
        return withContext(Dispatchers.Default) {
            when (image) {
                is PlatformBitmapImage -> {
                    processImage(image.nativeImage)
                }

                is PlatformImageProxy -> {
                    processImage(image.nativeImage)
                }

                is PlatformStreamImage -> {
                    val streamImage = image.nativeImage
                    val inputImage = MLKVisionImage(streamImage).apply {
                        setOrientation(image.orientation)
                    }
                    processImage(inputImage)
                }
            }
        }
    }


    private fun buildOptions(options: PoseDetectorOptions): MLKPoseDetectorOptions {
      return  MLKPoseDetectorOptions().apply {
            setDetectorMode(
                if (options.detectorMode == PoseDetectorOptions.STREAM_MODE) {
                    MLKPoseDetectorModeStream
                } else {
                    MLKPoseDetectorModeSingleImage
                }
            )
        }
    }

    @Suppress("UNCHECKED_CAST_TO_FORWARD_DECLARATION")
    private suspend fun processImage(nativeImage: MLKVisionImage): Result<List<Pose>> {
        return suspendCancellableCoroutine { emitter ->
            poseDetection.processImage(nativeImage as objcnames.protocols.MLKCompatibleImageProtocol) { pose, error ->
                if (pose != null) {
                    if (pose.isEmpty()) {
                        emitter.resume(Result.failure(Throwable("No pose found!")))
                    } else {
                        val result = parsePose(pose)
                        emitter.resume(Result.success(result))
                    }
                } else {
                    emitter.resume(Result.failure(Throwable(error?.debugDescription)))
                }
            }
        }
    }

    private fun parsePose(pose: List<*>): List<Pose> {
        return pose.map {
            it as MLKPose
            val landmarks = it.landmarks.map { l ->
                l as MLKPoseLandmark
                PoseLandmark(
                    inFrameLikelihood = l.inFrameLikelihood,
                    position = l.position.toMLKitPoint(),
                    position3D = l.position.toMLKitPoint3D(),
                    landmarkType = l.type.toMlKitType()
                )
            }
            Pose(poseLandmarks = landmarks)
        }
    }

    private fun MLKVision3DPoint.toMLKitPoint3D(): Point3D {
        this as cocoapods.GoogleMLKit.MLKVision3DPoint
        return Point3D(x = x().toFloat(), y = y().toFloat(), z = z().toFloat())
    }

    private fun MLKVision3DPoint.toMLKitPoint(): Point {
        this as cocoapods.GoogleMLKit.MLKVision3DPoint
        return Point(x = x().toFloat(), y = y().toFloat())
    }

    private fun MLKPoseLandmarkType.toMlKitType(): Int {
        return when (this) {
            MLKPoseLandmarkTypeNose -> PoseLandmark.NOSE
            MLKPoseLandmarkTypeLeftEyeInner -> PoseLandmark.LEFT_EYE_INNER
            MLKPoseLandmarkTypeLeftEye -> PoseLandmark.LEFT_EYE
            MLKPoseLandmarkTypeLeftEyeOuter -> PoseLandmark.LEFT_EYE_OUTER
            MLKPoseLandmarkTypeRightEyeInner -> PoseLandmark.RIGHT_EYE_INNER
            MLKPoseLandmarkTypeRightEye -> PoseLandmark.RIGHT_EYE
            MLKPoseLandmarkTypeRightEyeOuter -> PoseLandmark.RIGHT_EYE_OUTER
            MLKPoseLandmarkTypeLeftEar -> PoseLandmark.LEFT_EAR
            MLKPoseLandmarkTypeRightEar -> PoseLandmark.RIGHT_EAR
            MLKPoseLandmarkTypeMouthLeft -> PoseLandmark.LEFT_MOUTH
            MLKPoseLandmarkTypeMouthRight -> PoseLandmark.RIGHT_MOUTH
            MLKPoseLandmarkTypeLeftShoulder -> PoseLandmark.LEFT_SHOULDER
            MLKPoseLandmarkTypeRightShoulder -> PoseLandmark.RIGHT_SHOULDER
            MLKPoseLandmarkTypeLeftElbow -> PoseLandmark.LEFT_ELBOW
            MLKPoseLandmarkTypeRightElbow -> PoseLandmark.RIGHT_ELBOW
            MLKPoseLandmarkTypeLeftWrist -> PoseLandmark.LEFT_WRIST
            MLKPoseLandmarkTypeRightWrist -> PoseLandmark.RIGHT_WRIST
            MLKPoseLandmarkTypeLeftPinkyFinger -> PoseLandmark.LEFT_PINKY
            MLKPoseLandmarkTypeRightPinkyFinger -> PoseLandmark.RIGHT_PINKY
            MLKPoseLandmarkTypeLeftIndexFinger -> PoseLandmark.LEFT_INDEX
            MLKPoseLandmarkTypeRightIndexFinger -> PoseLandmark.RIGHT_INDEX
            MLKPoseLandmarkTypeLeftThumb -> PoseLandmark.LEFT_THUMB
            MLKPoseLandmarkTypeRightThumb -> PoseLandmark.RIGHT_THUMB
            MLKPoseLandmarkTypeLeftHip -> PoseLandmark.LEFT_HIP
            MLKPoseLandmarkTypeRightHip -> PoseLandmark.RIGHT_HIP
            MLKPoseLandmarkTypeLeftKnee -> PoseLandmark.LEFT_KNEE
            MLKPoseLandmarkTypeRightKnee -> PoseLandmark.RIGHT_KNEE
            MLKPoseLandmarkTypeLeftAnkle -> PoseLandmark.LEFT_ANKLE
            MLKPoseLandmarkTypeRightAnkle -> PoseLandmark.RIGHT_ANKLE
            MLKPoseLandmarkTypeLeftHeel -> PoseLandmark.LEFT_HEEL
            MLKPoseLandmarkTypeRightHeel -> PoseLandmark.RIGHT_HEEL
            MLKPoseLandmarkTypeLeftToe -> PoseLandmark.LEFT_FOOT_INDEX
            MLKPoseLandmarkTypeRightToe -> PoseLandmark.RIGHT_FOOT_INDEX
            else -> -1
        }
    }
}




