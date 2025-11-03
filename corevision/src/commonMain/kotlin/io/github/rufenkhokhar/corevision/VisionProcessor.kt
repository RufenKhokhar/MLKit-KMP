package io.github.rufenkhokhar.corevision

import io.github.rufenkhokhar.core.Processor

/**
 * An interface for processing visual data from a [PlatformImage].
 *
 * This interface extends the base [Processor] and is specialized for computer vision tasks.
 * Implementations of this interface are designed to analyze image frames and return a result of type [T].
 *
 * @param T The type of the result produced by the vision processing task (e.g., a list of barcodes, faces, or text blocks).
 */
interface VisionProcessor<T> : Processor<T> {
    /**
     * Processes a single image frame to detect features and returns the result.
     *
     * This is a suspending function, making it suitable for asynchronous execution on a background thread
     * to avoid blocking the UI.
     *
     * @param image The [PlatformImage] to be analyzed.
     * @return A [Result] object that encapsulates either the successful processing result of type [T] or an exception if an error occurs.
     */
    suspend fun processImageFrame(image: PlatformImage): Result<T>
}
