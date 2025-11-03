package io.github.rufenkhokhar.core
/**
 * A generic base interface for a processor that handles a specific task.
 *
 * This interface serves as a common contract for different types of processors
 * within the library. It is designed to be extended by more specialized interfaces
 * that define the actual processing logic.
 *
 * @param T The type of the result that the processor will produce.
 */
interface Processor<T>