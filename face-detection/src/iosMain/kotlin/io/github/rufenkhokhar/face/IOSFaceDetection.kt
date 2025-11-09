package io.github.rufenkhokhar.face

import cocoapods.GoogleMLKit.MLKFace
import cocoapods.GoogleMLKit.MLKFaceContour
import cocoapods.GoogleMLKit.MLKFaceContourType
import cocoapods.GoogleMLKit.MLKFaceContourTypeFace
import cocoapods.GoogleMLKit.MLKFaceContourTypeLeftEye
import cocoapods.GoogleMLKit.MLKFaceContourTypeLeftEyebrowBottom
import cocoapods.GoogleMLKit.MLKFaceContourTypeLeftEyebrowTop
import cocoapods.GoogleMLKit.MLKFaceContourTypeLowerLipBottom
import cocoapods.GoogleMLKit.MLKFaceContourTypeLowerLipTop
import cocoapods.GoogleMLKit.MLKFaceContourTypeNoseBottom
import cocoapods.GoogleMLKit.MLKFaceContourTypeNoseBridge
import cocoapods.GoogleMLKit.MLKFaceContourTypeRightEye
import cocoapods.GoogleMLKit.MLKFaceContourTypeRightEyebrowBottom
import cocoapods.GoogleMLKit.MLKFaceContourTypeRightEyebrowTop
import cocoapods.GoogleMLKit.MLKFaceContourTypeUpperLipBottom
import cocoapods.GoogleMLKit.MLKFaceContourTypeUpperLipTop
import cocoapods.GoogleMLKit.MLKFaceDetector
import cocoapods.GoogleMLKit.MLKFaceDetectorOptions
import cocoapods.GoogleMLKit.MLKFaceLandmark
import cocoapods.GoogleMLKit.MLKFaceLandmarkType
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeLeftCheek
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeLeftEar
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeLeftEye
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeMouthBottom
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeMouthLeft
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeMouthRight
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeNoseBase
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeRightCheek
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeRightEar
import cocoapods.GoogleMLKit.MLKFaceLandmarkTypeRightEye
import cocoapods.GoogleMLKit.MLKVisionImage
import cocoapods.GoogleMLKit.MLKVisionPoint
import io.github.rufenkhokhar.corevision.BoundingBox
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import io.github.rufenkhokhar.corevision.Point
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import objcnames.protocols.MLKCompatibleImageProtocol
import platform.CoreGraphics.CGRectGetMaxX
import platform.CoreGraphics.CGRectGetMaxY
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
internal class IOSFaceDetection(private val options: FaceDetectorOptions) : FaceDetection {

    private val faceDetection = MLKFaceDetector.faceDetectorWithOptions(buildOptions())

    override suspend fun processImageFrame(image: PlatformImage): Result<List<Face>> {
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

    private fun buildOptions(): MLKFaceDetectorOptions {
        return MLKFaceDetectorOptions().apply {
            minFaceSize = options.minFaceSize.toDouble()
            performanceMode = options.performanceMode.toLong()
            landmarkMode = options.landMarkMode.toLong()
            contourMode = options.contourMode.toLong()
            trackingEnabled = options.enableTracking
            classificationMode = options.classificationMode.toLong()
        }
    }

    @Suppress("UNCHECKED_CAST_TO_FORWARD_DECLARATION")
    private suspend fun processImage(nativeImage: MLKVisionImage): Result<List<Face>> {
        return suspendCoroutine { emitter ->
            faceDetection.processImage(nativeImage as MLKCompatibleImageProtocol) { faces, error ->
                if (faces != null) {
                    val result = faces.map { face ->
                        face as MLKFace
                        parseFace(face)
                    }
                    emitter.resume(Result.success(result))
                } else {
                    emitter.resume(Result.failure(Throwable(error?.debugDescription)))
                }
            }
        }
    }

    private fun parseFace(face: MLKFace): Face {
        val frame = face.frame
        val boundingBox = frame.useContents {
            BoundingBox(
                left = origin.x.toFloat(),
                top = origin.y.toFloat(),
                right = CGRectGetMaxX(frame).toFloat(),
                bottom = CGRectGetMaxY(frame).toFloat()
            )
        }
        val landmarks = Landmarks(
            mouthLeft = face.visionPointOrNull(MLKFaceLandmarkTypeMouthLeft),
            mouthRight = face.visionPointOrNull(MLKFaceLandmarkTypeMouthRight),
            mouthBottom = face.visionPointOrNull(MLKFaceLandmarkTypeMouthBottom),
            leftEye = face.visionPointOrNull(MLKFaceLandmarkTypeLeftEye),
            rightEye = face.visionPointOrNull(MLKFaceLandmarkTypeRightEye),
            leftCheek = face.visionPointOrNull(MLKFaceLandmarkTypeLeftCheek),
            rightCheek = face.visionPointOrNull(MLKFaceLandmarkTypeRightCheek),
            noseBase = face.visionPointOrNull(MLKFaceLandmarkTypeNoseBase),
            leftEar = face.visionPointOrNull(MLKFaceLandmarkTypeLeftEar),
            rightEar = face.visionPointOrNull(MLKFaceLandmarkTypeRightEar),
            leftEarTip = null,       // not exposed on iOS
            rightEarTip = null
        )

        val contours = Contours(
            face = face.contourPoints(MLKFaceContourTypeFace),
            leftEyebrowTop = face.contourPoints(MLKFaceContourTypeLeftEyebrowTop),
            leftEyebrowBottom = face.contourPoints(MLKFaceContourTypeLeftEyebrowBottom),
            rightEyebrowTop = face.contourPoints(MLKFaceContourTypeRightEyebrowTop),
            rightEyebrowBottom = face.contourPoints(MLKFaceContourTypeRightEyebrowBottom),
            leftEye = face.contourPoints(MLKFaceContourTypeLeftEye),
            rightEye = face.contourPoints(MLKFaceContourTypeRightEye),
            upperLipTop = face.contourPoints(MLKFaceContourTypeUpperLipTop),
            upperLipBottom = face.contourPoints(MLKFaceContourTypeUpperLipBottom),
            lowerLipTop = face.contourPoints(MLKFaceContourTypeLowerLipTop),
            lowerLipBottom = face.contourPoints(MLKFaceContourTypeLowerLipBottom),
            noseBridge = face.contourPoints(MLKFaceContourTypeNoseBridge),
            noseBottom = face.contourPoints(MLKFaceContourTypeNoseBottom)
        )

        val leftEyeProb = if (face.hasLeftEyeOpenProbability) face.leftEyeOpenProbability.toFloat()
            .orNullIfNaN() else null
        val rightEyeProb =
            if (face.hasRightEyeOpenProbability) face.rightEyeOpenProbability.toFloat()
                .orNullIfNaN() else null
        val smileProb = if (face.hasSmilingProbability) face.smilingProbability.toFloat()
            .orNullIfNaN() else null


        return Face(
            boundingBox = boundingBox,
            headEulerAngleX = face.headEulerAngleX.toFloat(),
            headEulerAngleY = face.headEulerAngleY.toFloat(),
            headEulerAngleZ = face.headEulerAngleZ.toFloat(),
            leftEyeOpenProbability = leftEyeProb,
            rightEyeOpenProbability = rightEyeProb,
            smilingProbability = smileProb,
            trackingId = face.trackingID.toInt(),
            landmarks = landmarks,
            contours = contours
        )
    }

    private fun Float?.orNullIfNaN(): Float? = this?.takeIf { !it.isNaN() }


    private fun MLKFace.visionPointOrNull(type: MLKFaceLandmarkType): Point? {
        val landmark: MLKFaceLandmark? = landmarkOfType(type)
        @Suppress("CAST_NEVER_SUCCEEDS")
        val point: MLKVisionPoint = landmark?.position() as? MLKVisionPoint ?: return null
        point.x
        return Point(x = point.x.toInt(), point.y.toInt())

    }

    private fun MLKFace.contourPoints(type: MLKFaceContourType): List<Point> {
        val contour: MLKFaceContour? = contourOfType(type)
        val arr = contour?.points ?: return emptyList()
        return arr.mapNotNull {
            val p = it as? MLKVisionPoint
            p?.let { Point(p.x.toInt(), p.y.toInt()) }
        }
    }
}





