package com.example.audio

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AudioStorageManager {

    fun generateUniqueFilename(extension: String = "wav"): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = dateFormat.format(Date())
        return "VoiceCraft_$timestamp.$extension"
    }

    fun getAudioCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "voicecraft_audio")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createCacheAudioFile(context: Context, extension: String = "wav"): File {
        val filename = generateUniqueFilename(extension)
        return File(getAudioCacheDir(context), filename)
    }

    fun doesAudioExist(filePath: String): Boolean {
        if (filePath.isBlank()) return false
        val file = File(filePath)
        return file.exists() && file.length() > 0
    }

    /**
     * Downloads/Exports generated audio to Android public Music/Downloads folder
     * using scoped MediaStore API (no storage permissions required on API 29+).
     * Falls back to app external music directory if MediaStore is restricted.
     */
    fun exportToPublicStorage(
        context: Context,
        sourceFile: File,
        customTitle: String? = null
    ): Result<Uri> {
        return try {
            if (!sourceFile.exists()) {
                return Result.failure(IllegalStateException("Audio file does not exist"))
            }

            val ext = if (sourceFile.extension.equals("mp3", ignoreCase = true)) "mp3" else "wav"
            val mimeType = if (ext == "mp3") "audio/mpeg" else "audio/wav"

            val filename = if (!customTitle.isNullOrBlank()) {
                val clean = customTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
                "VoiceCraft_${clean}_$dateFormat.$ext"
            } else {
                generateUniqueFilename(ext)
            }

            // Attempt MediaStore scoped insertion (standard for Android 10+ / API 29+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                    put(MediaStore.Audio.Media.TITLE, customTitle ?: "VoiceCraft Audio")
                    put(MediaStore.Audio.Media.ARTIST, "VoiceCraft AI")
                    put(MediaStore.Audio.Media.ALBUM, "VoiceCraft Creations")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/VoiceCraft")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }

                val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val uri = resolver.insert(collection, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        FileInputStream(sourceFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    contentValues.clear()
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    return Result.success(uri)
                }
            }

            // Fallback for API 26-28 or if MediaStore insert returned null
            val externalMusicDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                ?: context.filesDir
            val destDir = File(externalMusicDir, "VoiceCraft").apply { if (!exists()) mkdirs() }
            val destFile = File(destDir, filename)

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                destFile
            )

            Result.success(contentUri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates an Intent to share the audio file using FileProvider.
     */
    fun createShareIntent(context: Context, audioFile: File, scriptTitle: String?): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            audioFile
        )

        return Intent(Intent.ACTION_SEND).apply {
            type = "audio/wav"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, scriptTitle ?: "VoiceCraft AI Audio")
            putExtra(
                Intent.EXTRA_TEXT,
                "Listen to this AI voiceover generated with VoiceCraft AI: ${scriptTitle ?: "Audio"}"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
