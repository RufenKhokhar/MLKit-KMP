@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.corevision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage

/**
 * Wraps a CameraX frame as a platform image for ML processing.
 *
 * Use this when you receive frames from `ImageAnalysis.Analyzer`. This avoids
 * converting to `Bitmap` and keeps things zero-copy where possible.
 *
 * Internally:
 * - Holds the `ImageProxy` and defers YUV conversion to the consumer.
 *
 * Notes:
 * - Close the returned `ImageProxy` when processing is finished to avoid stalling the camera pipeline.
 * - If your detector needs rotation metadata, pass it through your `PlatformImage` implementation
 *   or account for `imageProxy.imageInfo.rotationDegrees` downstream.
 */
@OptIn(ExperimentalGetImage::class)
fun ImageProxy.toPlatformImage(): PlatformImage {
    return PlatformImageProxy(this)
}

/**
 * Wraps a `Bitmap` as a platform image for ML processing.
 *
 * Use this when you already have a `Bitmap` in memory (e.g., loaded from disk,
 * gallery, or produced by your pipeline).
 *
 * @param rotationDegrees Clockwise rotation to apply, in degrees. Common values are 0, 90, 180, 270.
 *                        ML Kit uses this to interpret the image upright. If you got the bitmap
 *                        from CameraX, prefer using the `ImageProxy` overload and pass rotation there.
 *
 * @return `PlatformImage` backed by `InputImage.fromBitmap`.
 *
 * Notes:
 * - `rotationDegrees` is metadata only. Pixels are not rotated in memory.
 * - If your source has EXIF orientation, read and convert it to degrees before calling.
 */
fun Bitmap.toPlatformImage(rotationDegrees: Int = 0): PlatformImage {
    return PlatformBitmapImage(InputImage.fromBitmap(this, rotationDegrees))
}

/**
 * Decodes this byte array into a `Bitmap` and wraps it as a platform image.
 *
 * Use this for JPEG/PNG/WebP bytes in memory. This allocates a `Bitmap`, so it
 * is less efficient than using `ImageProxy` for live camera frames.
 *
 * @param rotationDegrees Clockwise rotation to apply as metadata for ML Kit.
 * @return `PlatformImage` backed by a decoded `Bitmap`.
 *
 * @throws IllegalArgumentException if the byte array cannot be decoded into a `Bitmap`.
 *
 * Notes:
 * - For large images, consider decoding with `BitmapFactory.Options.inSampleSize` to reduce memory.
 * - If bytes come from a camera capture, prefer passing an `ImageProxy` to avoid an extra decode.
 */
fun ByteArray.toPlatformImage(rotationDegrees: Int = 0): PlatformImage {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        ?: error("Failed to decode Bitmap from byte array")
    return bitmap.toPlatformImage(rotationDegrees)
}

/**
 * Loads an image from a `Uri` and wraps it as a platform image.
 *
 * Use this for content Uris from Storage Access Framework, MediaStore, or file Uris.
 * ML Kit reads the stream behind the scenes and attaches the correct metadata.
 *
 * @param context A `Context` with permission to read the Uri.
 * @return `PlatformImage` backed by `InputImage.fromFilePath`.
 *
 * @throws IOException if the Uri cannot be opened or read.
 *
 * Notes:
 * - This does not rotate pixels. If the Uri has EXIF orientation, ML Kit reads and applies it.
 * - On Android 10+ with scoped storage, ensure you have access to the Uri (persistable permissions if needed).
 */
fun Uri.toPlatformImage(context: Context): PlatformImage {
    return PlatformStreamImage(InputImage.fromFilePath(context, this))
}


actual sealed class PlatformImage
actual class PlatformImageProxy internal constructor(val nativeImage: ImageProxy) : PlatformImage()
actual class PlatformBitmapImage internal constructor(val nativeImage: InputImage): PlatformImage()

actual class PlatformStreamImage internal constructor(val nativeImage: InputImage) : PlatformImage()

actual fun PlatformImage.close() {
    if (this is PlatformImageProxy) {
        println("Closing image proxy!" )
        nativeImage.close()
    }else{
        println("Closing image stream!" )
    }
}