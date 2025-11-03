@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.github.rufenkhokhar.corevision

/**
 * A sealed class representing a platform-specific image that can be processed by vision detectors.
 *
 * This class serves as a common abstraction for different image representations across various platforms
 * (e.g., Android, iOS), allowing image processing logic to be shared in common code.
 * Concrete implementations will be one of its subclasses like [PlatformImageProxy], [PlatformBitmapImage], or [PlatformStreamImage].
 */
expect sealed class PlatformImage

/**
 * Represents a platform-specific image that is provided as an image proxy object.
 *
 * This is often used for real-time processing of camera frames, where direct memory access
 * can improve performance and reduce copying overhead. It is a common format in camera APIs.
 */
expect class PlatformImageProxy : PlatformImage

/**
 * Represents a platform-specific image from a bitmap.
 *
 * This is typically used for processing static images that are already decoded into a bitmap format
 * (e.g., from an image file, a canvas, or other graphics buffer).
 */
expect class PlatformBitmapImage : PlatformImage

/**
 * Represents a platform-specific image from a stream of bytes.
 *
 * This is useful for processing images from a file, a network response, or any other byte stream
 * without fully decoding it into a bitmap first, which can be more memory-efficient.
 */
expect class PlatformStreamImage


/**
 * Closes the underlying image resource to release memory.
 *
 * It is crucial to call this method when the image is no longer needed, especially when dealing with
 * real-time frames from a camera ([PlatformImageProxy]) or large image files, to prevent memory leaks.
 */
expect fun PlatformImage.close()



