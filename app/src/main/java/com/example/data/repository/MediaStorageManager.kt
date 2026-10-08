package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class MediaStorageManager(private val context: Context) {

    private val mediaBaseDir: File
        get() = File(context.filesDir, "media").apply { if (!exists()) mkdirs() }

    private val downloadsBaseDir: File
        get() = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }

    suspend fun saveMediaFromUri(
        uri: Uri,
        subfolder: String,
        prefix: String = "upload",
        onProgress: (Float) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val targetDir = File(mediaBaseDir, subfolder).apply { if (!exists()) mkdirs() }
            val originalName = queryFileName(uri) ?: "media_${System.currentTimeMillis()}"
            val extension = originalName.substringAfterLast('.', "")
            val safeExtension = if (extension.isNotEmpty()) ".$extension" else ""
            val destFile = File(targetDir, "${prefix}_${UUID.randomUUID()}$safeExtension")

            val totalBytes = queryFileSize(uri)
            var copiedBytes = 0L

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        copiedBytes += read
                        if (totalBytes > 0) {
                            val progress = (copiedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                            onProgress(progress)
                        } else {
                            onProgress(0.5f)
                        }
                    }
                    output.flush()
                }
            } ?: return@withContext Result.failure(Exception("Cannot open file stream"))

            onProgress(1.0f)
            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadMediaFile(
        sourceUrlOrPath: String,
        subfolder: String = "offline",
        prefix: String = "download",
        onProgress: (Int) -> Unit
    ): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        try {
            val targetDir = File(downloadsBaseDir, subfolder).apply { if (!exists()) mkdirs() }
            val destFile = File(targetDir, "${prefix}_${UUID.randomUUID()}.mp4")

            if (sourceUrlOrPath.startsWith("http://", ignoreCase = true) ||
                sourceUrlOrPath.startsWith("https://", ignoreCase = true)
            ) {
                val url = URL(sourceUrlOrPath)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 30_000
                    instanceFollowRedirects = true
                }
                connection.connect()

                if (connection.responseCode !in 200..299) {
                    return@withContext Result.failure(Exception("HTTP error ${connection.responseCode}"))
                }

                val totalLength = connection.contentLengthLong
                var downloadedBytes = 0L

                connection.inputStream.use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(32 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            if (totalLength > 0) {
                                val percent = ((downloadedBytes * 100) / totalLength).toInt().coerceIn(0, 99)
                                onProgress(percent)
                            }
                        }
                        output.flush()
                    }
                }
            } else {
                // Local file or URI copy to download storage
                val srcFile = File(sourceUrlOrPath)
                if (srcFile.exists()) {
                    val totalLength = srcFile.length()
                    var downloadedBytes = 0L
                    srcFile.inputStream().use { input ->
                        FileOutputStream(destFile).use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                downloadedBytes += read
                                if (totalLength > 0) {
                                    val percent = ((downloadedBytes * 100) / totalLength).toInt().coerceIn(0, 99)
                                    onProgress(percent)
                                }
                            }
                            output.flush()
                        }
                    }
                } else {
                    val uri = Uri.parse(sourceUrlOrPath)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: return@withContext Result.failure(Exception("File source not found: $sourceUrlOrPath"))
                }
            }

            onProgress(100)
            Result.success(Pair(destFile.absolutePath, destFile.length()))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        return name ?: uri.lastPathSegment
    }

    private fun queryFileSize(uri: Uri): Long {
        var size = -1L
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        return size
    }

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 MB"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format("%.2f GB", gb)
                mb >= 1.0 -> String.format("%.1f MB", mb)
                else -> String.format("%.0f KB", kb)
            }
        }
    }
}
