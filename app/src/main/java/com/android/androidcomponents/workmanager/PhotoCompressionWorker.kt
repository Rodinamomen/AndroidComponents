package com.android.androidcomponents.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

/**
 * PhotoCompressionWorker is a background worker built on top of Android's WorkManager library.
 *
 * ## What is WorkManager?
 * WorkManager is a Jetpack library that allows you to schedule and run background tasks that need
 * to be executed reliably, even if the app is killed or the device restarts. Unlike regular
 * coroutines or threads, WorkManager persists the task in a local database and guarantees its
 * execution once the specified constraints are met (e.g., network available, device charging).
 * It is the recommended solution for any task that is:
 *  - Deferrable (doesn't need to run immediately)
 *  - Guaranteed (must complete even if the app exits)
 *
 * ## What does this worker do?
 * This worker receives a content URI pointing to an image, then compresses it iteratively
 * by reducing the JPEG quality step by step until the file size falls below a given threshold.
 * The compressed image is saved to the app's cache directory, and its path is returned as output.
 *
 * ## Why CoroutineWorker?
 * We extend [CoroutineWorker] instead of the basic [androidx.work.Worker] because it has native
 * support for Kotlin coroutines, allowing us to use suspend functions and switch dispatchers
 * without blocking any thread.
 *
 * @param appContext The application context provided by WorkManager.
 * @param params Parameters containing input data and metadata about this work request.
 */
class PhotoCompressionWorker(
    private val appContext: Context,
    private val params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    /**
     * The main entry point of the worker, called automatically by WorkManager when it is
     * time to execute the task. This function must return a [Result] that tells WorkManager
     * whether the work was successful, failed, or should be retried.
     *
     * All work is dispatched on [Dispatchers.IO] since we are performing heavy I/O operations
     * (reading streams, compressing bitmaps, writing files).
     *
     * @return [Result.success] with the output file path if compression succeeded,
     *         or [Result.failure] if the input URI was invalid or the stream could not be opened.
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {

            // Retrieve the image URI string and compression threshold passed as input data
            // when the WorkRequest was built. Input data is a key-value bundle (like a Map).
            val stringUri = params.inputData.getString(KEY_CONTENT_URI)
            val compressionThresholdInBytes = params.inputData.getLong(
                KEY_COMPRESSION_THRESHOLD,
                0L // Default to 0 if not provided
            )

            // Parse the URI string back into a Uri object so we can open an InputStream from it
            val uri = Uri.parse(stringUri)

            // Open an InputStream from the content resolver using the URI.
            // The content resolver acts as a bridge to access files from other apps or the system
            // (e.g., a photo from the Gallery). If the stream cannot be opened, we fail early.
            val bytes = appContext.contentResolver.openInputStream(uri)?.use {
                it.readBytes() // Read the entire image into a byte array
            } ?: return@withContext Result.failure() // URI was invalid or inaccessible

            // Decode the raw bytes into a Bitmap object so we can re-compress it at a lower quality
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            var outputBytes: ByteArray
            var quality = 100 // Start at maximum JPEG quality (100%)

            // Iteratively compress the bitmap, reducing quality by 10% each iteration,
            // until the output size is within the threshold OR quality drops below 5%.
            // This avoids an infinite loop while still trying hard to meet the size target.
            do {
                val outputStream = ByteArrayOutputStream()
                outputStream.use { outputStream ->
                    // Compress the bitmap as JPEG at the current quality into the stream
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                    outputBytes = outputStream.toByteArray()

                    // Reduce quality by 10% of its current value for the next iteration.
                    // Using a relative reduction (rather than a fixed step) makes the quality
                    // drop faster at higher values and slower near the lower bound.
                    quality -= (quality * 0.1).roundToInt()
                }
            } while (outputBytes.size > compressionThresholdInBytes && quality > 5)

            // Save the final compressed bytes to a file in the app's private cache directory.
            // We use the unique work request ID as the filename to avoid collisions
            // when multiple compression tasks run concurrently.
            val file = File(appContext.cacheDir, "${params.id}.jpg")
            file.writeBytes(outputBytes)

            // Return success and attach the output file path so that the caller
            // (or a chained worker) can read and use the compressed image.
            Result.success(
                workDataOf(KEY_RESULT_PATH to file.absolutePath)
            )
        }
    }

    companion object {
        /** Input key: the content URI of the original image to be compressed (as a String). */
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"

        /** Input key: the maximum allowed file size in bytes after compression (as a Long). */
        const val KEY_COMPRESSION_THRESHOLD = "KEY_COMPRESSION_THRESHOLD"

        /** Output key: the absolute file path of the compressed image saved in the cache (as a String). */
        const val KEY_RESULT_PATH = "KEY_RESULT_PATH"
    }
}