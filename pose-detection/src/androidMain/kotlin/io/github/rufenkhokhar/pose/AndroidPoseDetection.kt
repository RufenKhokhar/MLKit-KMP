package io.github.rufenkhokhar.pose

import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import io.github.rufenkhokhar.corevision.toMLKitPoint
import io.github.rufenkhokhar.corevision.toMLKitPoint3D
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


internal class AndroidPoseDetection(options: PoseDetectorOptions) : PoseDetection {

    private val poseDetection =
        com.google.mlkit.vision.pose.PoseDetection.getClient(createOptions(options))

    private fun createOptions(options: PoseDetectorOptions): PoseDetectorOptionsBase {
        return com.google.mlkit.vision.pose.defaults.PoseDetectorOptions.Builder()
            .setDetectorMode(options.detectorMode)
            .build()
    }

    @ExperimentalGetImage
    override suspend fun processImageFrame(image: PlatformImage): Result<List<Pose>> {
        return withContext(Dispatchers.Default) {
            when (image) {
                is PlatformBitmapImage -> {
                    processImage(image.nativeImage)
                }

                is PlatformImageProxy -> {
                    val imageProxy = image.nativeImage
                    val inputImage = InputImage.fromMediaImage(
                        imageProxy.image!!,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    processImage(inputImage)
                }

                is PlatformStreamImage -> {
                    processImage(image.nativeImage)
                }
            }
        }
    }

    private suspend fun processImage(inputImage: InputImage): Result<List<Pose>> {
        return suspendCancellableCoroutine { emitter ->
            poseDetection.process(inputImage)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        if (result.result.allPoseLandmarks.isEmpty()) {
                            emitter.resume(Result.failure(Throwable("No pose found!")))
                        } else {
                            val resultData = parsePose(result.result)
                            emitter.resume(Result.success(listOf(resultData)))
                        }
                    } else {
                        emitter.resume(Result.failure(Throwable(result.exception)))
                    }
                }
        }
    }

    private fun parsePose(result: com.google.mlkit.vision.pose.Pose): Pose {
        val landmarks = result.allPoseLandmarks.map { landmark ->
            PoseLandmark(
                inFrameLikelihood = landmark.inFrameLikelihood,
                position = landmark.position.toMLKitPoint(),
                position3D = landmark.position3D.toMLKitPoint3D(),
                landmarkType = landmark.landmarkType
            )
        }
        return Pose(poseLandmarks = landmarks)
    }
}