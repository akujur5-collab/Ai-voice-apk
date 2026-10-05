package com.example.audio

import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavUtils {

    /**
     * Checks if the given byte array is already a valid WAV file.
     */
    fun isWavFormat(data: ByteArray): Boolean {
        if (data.size < 12) return false
        return data[0] == 'R'.code.toByte() &&
               data[1] == 'I'.code.toByte() &&
               data[2] == 'F'.code.toByte() &&
               data[3] == 'F'.code.toByte() &&
               data[8] == 'W'.code.toByte() &&
               data[9] == 'A'.code.toByte() &&
               data[10] == 'V'.code.toByte() &&
               data[11] == 'E'.code.toByte()
    }

    /**
     * Checks if the given byte array is MP3 format.
     */
    fun isMp3Format(data: ByteArray): Boolean {
        if (data.size < 3) return false
        if (data[0] == 'I'.code.toByte() && data[1] == 'D'.code.toByte() && data[2] == '3'.code.toByte()) {
            return true
        }
        if (data.size >= 2) {
            val byte0 = data[0].toInt() and 0xFF
            val byte1 = data[1].toInt() and 0xFF
            if (byte0 == 0xFF && (byte1 and 0xE0) == 0xE0) return true
        }
        return false
    }

    /**
     * Parses sample rate from mimeType if present (e.g., "audio/pcm;rate=24000"), defaulting to 24000.
     */
    fun parseSampleRate(mimeType: String?, defaultRate: Int = 24000): Int {
        if (mimeType == null) return defaultRate
        if (mimeType.contains("rate=")) {
            val candidate = mimeType.substringAfter("rate=").takeWhile { it.isDigit() }.toIntOrNull()
            if (candidate != null && candidate in 8000..96000) {
                return candidate
            }
        }
        return defaultRate
    }

    /**
     * Converts 16-bit PCM byte data to standard RIFF/WAVE byte array.
     */
    fun pcmToWav(
        pcmData: ByteArray,
        sampleRate: Int = 24000,
        channels: Int = 1,
        bitsPerSample: Int = 16
    ): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteArray(44)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

        // "RIFF"
        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        // Overall size - 8
        buffer.putInt(totalDataLen)
        // "WAVE"
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())
        // "fmt " chunk
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // Subchunk1Size for PCM
        buffer.putShort(1.toShort()) // AudioFormat 1 = PCM
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())
        // "data" chunk
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(totalAudioLen)

        val output = ByteArrayOutputStream(44 + pcmData.size)
        output.write(header)
        output.write(pcmData)
        return output.toByteArray()
    }

    /**
     * Locates the start offset of PCM samples after the 'data' chunk header in a WAV file.
     */
    private fun findDataChunkOffset(bytes: ByteArray): Int {
        for (i in 12 until bytes.size - 8) {
            if (bytes[i] == 'd'.code.toByte() &&
                bytes[i + 1] == 'a'.code.toByte() &&
                bytes[i + 2] == 't'.code.toByte() &&
                bytes[i + 3] == 'a'.code.toByte()
            ) {
                return i + 8
            }
        }
        return 44.coerceAtMost(bytes.size)
    }

    /**
     * Concatenates multiple WAV files with identical audio parameters into a single WAV file.
     */
    fun concatenateWavFiles(inputFiles: List<File>, outputFile: File): Boolean {
        if (inputFiles.isEmpty()) return false
        if (inputFiles.size == 1) {
            inputFiles[0].copyTo(outputFile, overwrite = true)
            return true
        }

        try {
            val audioDataStream = ByteArrayOutputStream()
            var sampleRate = 24000
            var channels = 1
            var bitsPerSample = 16

            inputFiles.forEachIndexed { index, file ->
                val bytes = file.readBytes()
                if (isWavFormat(bytes)) {
                    if (index == 0 && bytes.size >= 36) {
                        val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
                        // Read fmt parameters if present
                        for (i in 12 until bytes.size - 16) {
                            if (bytes[i] == 'f'.code.toByte() &&
                                bytes[i + 1] == 'm'.code.toByte() &&
                                bytes[i + 2] == 't'.code.toByte() &&
                                bytes[i + 3] == ' '.code.toByte()
                            ) {
                                channels = buf.getShort(i + 10).toInt()
                                sampleRate = buf.getInt(i + 12)
                                bitsPerSample = buf.getShort(i + 22).toInt()
                                break
                            }
                        }
                    }
                    val dataOffset = findDataChunkOffset(bytes)
                    if (dataOffset < bytes.size) {
                        audioDataStream.write(bytes, dataOffset, bytes.size - dataOffset)
                    }
                } else {
                    audioDataStream.write(bytes)
                }
            }

            val finalWavBytes = pcmToWav(
                audioDataStream.toByteArray(),
                sampleRate = sampleRate,
                channels = channels,
                bitsPerSample = bitsPerSample
            )
            outputFile.writeBytes(finalWavBytes)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Concatenates multiple audio files into the target output file.
     * Supports both MP3 and WAV target formats.
     */
    fun concatenateAudioFiles(inputFiles: List<File>, outputFile: File, targetFormat: String = "MP3"): Boolean {
        if (inputFiles.isEmpty()) return false
        if (inputFiles.size == 1) {
            inputFiles[0].copyTo(outputFile, overwrite = true)
            return true
        }

        val isMp3Target = targetFormat.equals("MP3", ignoreCase = true)
        val firstBytes = try { inputFiles.first().readBytes() } catch (e: Exception) { ByteArray(0) }
        val isFirstMp3 = isMp3Format(firstBytes)

        if (isMp3Target && isFirstMp3) {
            // Concatenate MP3 frame stream
            return try {
                outputFile.outputStream().use { outStream ->
                    for (file in inputFiles) {
                        file.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        return concatenateWavFiles(inputFiles, outputFile)
    }
}
