package io.github.rufenkhokhar.text

import cocoapods.GoogleMLKit.MLKText
import cocoapods.GoogleMLKit.MLKTextRecognizer
import cocoapods.GoogleMLKit.MLKVisionImage
import cocoapods.GoogleMLKit.textRecognizer
import io.github.rufenkhokhar.corevision.PlatformBitmapImage
import io.github.rufenkhokhar.corevision.PlatformImage
import io.github.rufenkhokhar.corevision.PlatformImageProxy
import io.github.rufenkhokhar.corevision.PlatformStreamImage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import objcnames.protocols.MLKCompatibleImageProtocol
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
internal class IOSTextRecognition : TextRecognition {
    private val recognition = MLKTextRecognizer.textRecognizer()
    override suspend fun processImageFrame(image: PlatformImage): Result<VisionText> {
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

    @Suppress("UNCHECKED_CAST_TO_FORWARD_DECLARATION")
    private suspend fun processImage(nativeImage: MLKVisionImage): Result<VisionText> {
        return suspendCoroutine { emitter ->
            recognition.processImage(nativeImage as MLKCompatibleImageProtocol) { result, error ->
                if (result != null) {
                    val result = parseText(result)
                    emitter.resume(Result.success(result))
                } else {
                    emitter.resume(Result.failure(Throwable(error?.debugDescription)))
                }
            }
        }
    }

    fun parseText(text: MLKText): VisionText {
        return VisionText(
            text = text.text(),
            textBlocks = emptyList()
        )
    }
}




