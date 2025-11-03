@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.corevision

import cocoapods.GoogleMLKit.MLKVisionImage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.CoreMedia.CMSampleBufferRef
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageOrientation

/**
 * Builds a ML Kit-compatible image from a camera frame.
 *
 * Use this when you’re processing frames from `AVCaptureVideoDataOutput` on iOS.
 * The `orientation` you pass should reflect how the frame should be interpreted
 * (based on device orientation and camera position), otherwise detections may be rotated.
 *
 * Internally:
 * - Wraps the `CMSampleBufferRef` in an `MLKVisionImage`
 * - Applies the provided `UIImageOrientation` to keep results correct
 *
 * @param orientation The logical orientation for the buffer (e.g., from your camera pipeline).
 * @return A `PlatformImage` wrapper around an `MLKVisionImage`, ready for ML Kit detectors.
 *
 * @throws IllegalStateException (via downstream code) if wrapping fails unexpectedly.
 *
 * ### Notes
 * - Call this on the same thread you receive the buffer unless you’ve retained it properly.
 * - Prefer this over converting to `UIImage` for performance when working with live video.
 */
@OptIn(ExperimentalForeignApi::class)
fun CMSampleBufferRef.toPlatformImage(orientation: UIImageOrientation): PlatformImage {
    val inputImage = MLKVisionImage(this).apply {
        setOrientation(orientation)
    }
    return PlatformBitmapImage(inputImage)
}

/**
 * Builds a ML Kit-compatible image from a file-backed `NSURL`.
 *
 * Use this for local **file URLs** (e.g., Documents, Caches, app bundle paths you
 * converted to file URLs). This loads a `UIImage` directly from the file path, which
 * is memory-friendly for large images compared to reading the whole file into `NSData`.
 *
 * Internally:
 * - Loads `UIImage` via `UIImage.imageWithContentsOfFile`
 * - Wraps it in `MLKVisionImage`
 * - Copies the image’s EXIF orientation so ML Kit interprets it correctly
 *
 * @return A `PlatformImage` wrapper around an `MLKVisionImage`, ready for ML Kit detectors.
 *
 * @throws IllegalStateException if:
 * - The URL has no `.path` (not a file URL), or
 * - The image cannot be loaded from disk.
 *
 * ### When to use
 * - Local files you control (bundle, sandboxed storage)
 * - You want lower memory overhead than loading as `NSData`
 *
 * ### Avoid
 * - Non-file URLs (e.g., `assets-library://`, `ph://`). Use [toPlatformImageViaData] instead.
 */
@OptIn(ExperimentalForeignApi::class)
fun NSURL.toPlatformImage(): PlatformImage {
    val filePath = this.path ?: error("Invalid file URL: missing path")
    val uiImage = UIImage.imageWithContentsOfFile(filePath)
        ?: error("Failed to load UIImage from: $filePath")
   return uiImage.toPlatformImage()
}


/**
 * Builds a ML Kit-compatible image by first reading the URL into `NSData`.
 *
 * Use this if the URL is **not** a file URL or `imageWithContentsOfFile` won’t work
 * (e.g., Photos URLs, security-scoped bookmarks, or providers that require going
 * through `NSData`). This is more flexible but may use more memory for large files.
 *
 * Internally:
 * - Reads bytes via `NSData.dataWithContentsOfURL`
 * - Decodes a `UIImage` from the data
 * - Wraps it in `MLKVisionImage` and applies the source image orientation
 *
 * @return A `PlatformImage` wrapper around an `MLKVisionImage`, ready for ML Kit detectors.
 *
 * @throws IllegalStateException if:
 * - The URL cannot be read into `NSData`, or
 * - The data cannot be decoded into a `UIImage`.
 *
 * ### When to use
 * - Non-file URLs (Photos, document providers)
 * - You must go through data APIs for access
 *
 * ### Consider
 * - For very large images, prefer downscaling after decode if memory is tight.
 * - If you do have a plain file path, prefer [toPlatformImage] for lower overhead.
 */
@OptIn(ExperimentalForeignApi::class)
fun NSURL.toPlatformImageViaData(): PlatformImage {
    val data = NSData.dataWithContentsOfURL(this)
        ?: error("Failed to read data at: $this")
    return data.toPlatformImage()
}
/**
 * Builds an ML Kit–compatible image from raw image bytes (`NSData`).
 *
 * Use this when you have image data in memory (e.g., fetched over the network,
 * read from a file provider, or exported from Photos) and need to hand it to ML Kit.
 *
 * Internally:
 * - Decodes a `UIImage` from this `NSData`
 * - Wraps it in `MLKVisionImage`
 * - Applies the source image’s EXIF orientation so detections aren’t rotated
 *
 * @return A `PlatformImage` backed by an `MLKVisionImage`, ready for ML Kit detectors.
 *
 * @throws IllegalStateException if the data cannot be decoded to a `UIImage` (consider adding a null check if you need graceful failure).
 *
 * ### Notes
 * - For very large images, consider downscaling after decode if memory is tight.
 * - If you already have a `UIImage`, prefer [UIImage.toPlatformImage] to avoid re-decoding.
 */
@OptIn(ExperimentalForeignApi::class)
fun NSData.toPlatformImage(): PlatformImage {
    val uiImage = UIImage(data = this)
    return uiImage.toPlatformImage()
}

/**
 * Wraps a `UIImage` as an ML Kit–compatible image.
 *
 * Use this when you already hold a `UIImage` (from camera capture, Photos, or decoding)
 * and want to process it with ML Kit. This preserves the image’s EXIF orientation so
 * results aren’t rotated or mirrored incorrectly.
 *
 * Internally:
 * - Wraps the `UIImage` in `MLKVisionImage`
 * - Copies `imageOrientation` to the ML Kit image
 *
 * @return A `PlatformImage` backed by an `MLKVisionImage`, ready for ML Kit detectors.
 *
 * ### Notes
 * - Orientation is metadata only; pixels are not reoriented in memory.
 * - If you need to physically rotate/flip pixels for display, do that separately from ML inference.
 */
@OptIn(ExperimentalForeignApi::class)
fun UIImage.toPlatformImage(): PlatformImage {
    val visionImage = MLKVisionImage(this).apply {
        // Preserve EXIF orientation so ML Kit reads the image correctly
        setOrientation(imageOrientation)
    }
    return PlatformBitmapImage(visionImage)
}



/**
 * Builds a ML Kit–compatible image from a camera frame using `AVCaptureVideoOrientation`.
 *
 * Use this overload when you have the capture pipeline’s `AVCaptureVideoOrientation`
 * (from `AVCaptureConnection.videoOrientation`) and want ML Kit to interpret the frame upright.
 * It converts the capture orientation to `UIImageOrientation` and delegates to the buffer-based
 * initializer.
 *
 * @param orientation The capture orientation for the current frame (portrait, landscapeRight, etc.).
 * @return A `PlatformImage` that wraps an `MLKVisionImage`, ready for ML Kit detectors.
 *
 * ### Notes
 * - This does not handle *mirroring*. If you use the front camera and mirror the preview,
 *   keep in mind ML Kit sees the unmirrored buffer. Apply any horizontal flip in your
 *   downstream pipeline if your use case requires it.
 * - Make sure you pass the orientation from the same frame you’re analyzing, not a stale value.
 * - If you already computed a `UIImageOrientation`, call the other overload directly.
 *
 * @see toUIImageOrientation for the mapping between capture and image orientations.
 */
@OptIn(ExperimentalForeignApi::class)
fun CMSampleBufferRef.toPlatformImage(orientation: AVCaptureVideoOrientation): PlatformImage {
    return toPlatformImage(orientation.toUIImageOrientation())
}


actual sealed class PlatformImage

@OptIn(ExperimentalForeignApi::class)
actual class PlatformImageProxy internal constructor(val nativeImage: MLKVisionImage) : PlatformImage()


@OptIn(ExperimentalForeignApi::class)
actual class PlatformBitmapImage internal constructor(val nativeImage: MLKVisionImage) : PlatformImage()

@OptIn(ExperimentalForeignApi::class)
actual class PlatformStreamImage internal constructor(
    val nativeImage: CMSampleBufferRef,
    val orientation: UIImageOrientation
) : PlatformImage()

@OptIn(ExperimentalForeignApi::class)
actual fun PlatformImage.close() {
    when(this){
        is PlatformBitmapImage -> {
            nativeImage.finalize()
        }
        is PlatformImageProxy -> {
            nativeImage.finalize()
        }
        is PlatformStreamImage -> {
           // nativeImage
            // TODO: clean image reference
        }
    }
}