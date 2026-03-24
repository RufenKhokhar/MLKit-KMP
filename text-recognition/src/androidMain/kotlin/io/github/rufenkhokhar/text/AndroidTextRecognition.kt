package io.github.rufenkhokhar.text

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import io.github.rufenkhokhar.corevision.toBoundingBox
import io.github.rufenkhokhar.corevision.toMlKitPoints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalGetImage::class)
internal class AndroidTextRecognition : TextRecognition {
    private val recognizer = com.google.mlkit.vision.text.TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun processImageFrame(image: PlatformImage): Result<VisionText> {
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

    private suspend fun processImage(inputImage: InputImage): Result<VisionText> {
        return suspendCoroutine { emitter ->
            recognizer.process(inputImage)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        println("processImage -> texts:${result.result}")
                        val resultData = parseText(result.result)
                        emitter.resume(Result.success(resultData))
                    } else {
                        emitter.resume(Result.failure(Throwable(result.exception)))
                    }
                }
        }
    }

    private fun parseText(text: Text): VisionText {
        return VisionText(
            text = text.text,
            textBlocks = text.textBlocks.map { block ->
                TextBlock(
                    text = block.text,
                    boundingBox = block.boundingBox?.toBoundingBox(),
                    cornerPoints = block.cornerPoints?.toMlKitPoints() ?: emptyList(),
                    lines = block.lines.map { line ->
                        Line(
                            text = line.text,
                            boundingBox = line.boundingBox?.toBoundingBox(),
                            cornerPoints = line.cornerPoints?.toMlKitPoints() ?: emptyList(),
                            elements = line.elements.map { el ->
                                Element(
                                    text = el.text,
                                    boundingBox = el.boundingBox?.toBoundingBox(),
                                    cornerPoints = el.cornerPoints?.toMlKitPoints() ?: emptyList()
                                )
                            }
                        )
                    }
                )
            }
        )
    }


}