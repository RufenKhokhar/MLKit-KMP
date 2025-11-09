package io.github.rufenkhokhar.face

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceLandmark
import io.github.rufenkhokhar.corevision.BoundingBox
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import io.github.rufenkhokhar.corevision.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalGetImage::class)
internal class AndroidFaceDetection(private val options: FaceDetectorOptions) : FaceDetection {

    private val detector = com.google.mlkit.vision.face.FaceDetection.getClient(buildOptions())
    override suspend fun processImageFrame(image: PlatformImage): Result<List<Face>> {
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

    private suspend fun processImage(inputImage: InputImage): Result<List<Face>> {
        return suspendCoroutine { emitter ->
            detector.process(inputImage)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        println("processImage -> faces:${result.result}")
                        val resultData = result.result.map { face -> parseFace(face) }
                        emitter.resume(Result.success(resultData))
                    } else {
                        emitter.resume(Result.failure(Throwable(result.exception)))
                    }
                }
        }
    }

    private fun parseFace(face: com.google.mlkit.vision.face.Face): Face {
        val boundingBox = BoundingBox(
            left = face.boundingBox.left.toFloat(),
            top = face.boundingBox.top.toFloat(),
            right = face.boundingBox.right.toFloat(),
            bottom = face.boundingBox.bottom.toFloat()
        )

        return Face(
            boundingBox = boundingBox,
            headEulerAngleX = face.headEulerAngleX,
            headEulerAngleY = face.headEulerAngleY,
            headEulerAngleZ = face.headEulerAngleZ,
            leftEyeOpenProbability = face.leftEyeOpenProbability.orNullIfNaN(),
            rightEyeOpenProbability = face.rightEyeOpenProbability.orNullIfNaN(),
            smilingProbability = face.smilingProbability.orNullIfNaN(),
            trackingId = face.trackingId,
            landmarks = face.toLandmarks(),
            contours = face.toContours()
        )
    }


    private fun buildOptions(): com.google.mlkit.vision.face.FaceDetectorOptions {
        return com.google.mlkit.vision.face.FaceDetectorOptions.Builder()
            .setPerformanceMode(options.performanceMode)
            .setMinFaceSize(options.minFaceSize)
            .setClassificationMode(options.classificationMode)
            .setContourMode(options.contourMode)
            .setLandmarkMode(options.landMarkMode)
            .also {
                if (options.enableTracking) {
                    it.enableTracking()
                }
            }
            .build()
    }

    private fun Float?.orNullIfNaN(): Float? = this?.takeIf { !it.isNaN() }

    private fun com.google.mlkit.vision.face.Face.landmarkOrNull(type: Int): Point?{
       val position = getLandmark(type)?.position ?: return null
        return Point(
            x = position.x.toInt(),
            y = position.y.toInt()
        )
    }


    private fun com.google.mlkit.vision.face.Face.contourPointsOrEmpty(type: Int): List<Point> {
      val points =   getContour(type)?.points ?: emptyList()
        return points.map {
            Point(x = it.x.toInt(), y = it.y.toInt())
        }
    }


    private fun com.google.mlkit.vision.face.Face.toLandmarks(): Landmarks = Landmarks(
        mouthLeft = landmarkOrNull(FaceLandmark.MOUTH_LEFT),
        mouthRight = landmarkOrNull(FaceLandmark.MOUTH_RIGHT),
        mouthBottom = landmarkOrNull(FaceLandmark.MOUTH_BOTTOM),
        leftEye = landmarkOrNull(FaceLandmark.LEFT_EYE),
        rightEye = landmarkOrNull(FaceLandmark.RIGHT_EYE),
        leftCheek = landmarkOrNull(FaceLandmark.LEFT_CHEEK),
        rightCheek = landmarkOrNull(FaceLandmark.RIGHT_CHEEK),
        noseBase = landmarkOrNull(FaceLandmark.NOSE_BASE),
        leftEar = landmarkOrNull(FaceLandmark.LEFT_EAR),
        rightEar = landmarkOrNull(FaceLandmark.RIGHT_EAR),
        leftEarTip = landmarkOrNull(FaceLandmark.LEFT_EAR),
        rightEarTip = landmarkOrNull(FaceLandmark.RIGHT_EAR)
    )

    private fun com.google.mlkit.vision.face.Face.toContours(): Contours = Contours(
        face = contourPointsOrEmpty(FaceContour.FACE),
        leftEyebrowTop = contourPointsOrEmpty(FaceContour.LEFT_EYEBROW_TOP),
        leftEyebrowBottom = contourPointsOrEmpty(FaceContour.LEFT_EYEBROW_BOTTOM),
        rightEyebrowTop = contourPointsOrEmpty(FaceContour.RIGHT_EYEBROW_TOP),
        rightEyebrowBottom = contourPointsOrEmpty(FaceContour.RIGHT_EYEBROW_BOTTOM),
        leftEye = contourPointsOrEmpty(FaceContour.LEFT_EYE),
        rightEye = contourPointsOrEmpty(FaceContour.RIGHT_EYE),
        upperLipTop = contourPointsOrEmpty(FaceContour.UPPER_LIP_TOP),
        upperLipBottom = contourPointsOrEmpty(FaceContour.UPPER_LIP_BOTTOM),
        lowerLipTop = contourPointsOrEmpty(FaceContour.LOWER_LIP_TOP),
        lowerLipBottom = contourPointsOrEmpty(FaceContour.LOWER_LIP_BOTTOM),
        noseBridge = contourPointsOrEmpty(FaceContour.NOSE_BRIDGE),
        noseBottom = contourPointsOrEmpty(FaceContour.NOSE_BOTTOM)
    )

}