package com.example.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Voice
import com.example.data.model.VoiceStyle
import com.example.data.repository.TtsRepository
import com.example.network.NetworkClient
import com.example.network.model.AudioConfig
import com.example.network.model.BackendTtsRequest
import com.example.network.model.GeminiContent
import com.example.network.model.GeminiGenerateContentRequest
import com.example.network.model.GeminiGenerationConfig
import com.example.network.model.GeminiPart
import com.example.network.model.GeminiPrebuiltVoiceConfig
import com.example.network.model.GeminiSpeechConfig
import com.example.network.model.GeminiVoiceConfig
import com.example.network.model.SynthesisInput
import com.example.network.model.TextToSpeechRequest
import com.example.network.model.VoiceSelectionParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File

data class GeneratedAudioResult(
    val audioFile: File,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val engineUsed: String
)

class AudioSynthesisEngine(
    private val context: Context,
    val ttsRepository: TtsRepository = TtsRepository()
) {

    private val androidTtsEngine = AndroidTtsEngine(context)

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Splits long text intelligently along sentence and paragraph boundaries,
     * carefully preserving Hindi characters, danda (|), question marks, and punctuation.
     */
    fun splitTextIntoChunks(text: String, maxChunkLength: Int = 450): List<String> {
        val trimmed = text.trim()
        if (trimmed.length <= maxChunkLength) {
            return listOf(trimmed)
        }

        val chunks = mutableListOf<String>()
        // Split by paragraph first
        val paragraphs = trimmed.split(Regex("(?<=\\n\\n)|(?=\\n\\n)"))

        for (paragraph in paragraphs) {
            val pTrimmed = paragraph.trim()
            if (pTrimmed.isEmpty()) continue

            if (pTrimmed.length <= maxChunkLength) {
                chunks.add(pTrimmed)
            } else {
                // Split by sentences using English and Hindi punctuation (. ! ? । |)
                val sentenceRegex = Regex("(?<=[.!?।|\\n])\\s+")
                val sentences = pTrimmed.split(sentenceRegex)
                var currentChunk = StringBuilder()

                for (sentence in sentences) {
                    val sTrimmed = sentence.trim()
                    if (sTrimmed.isEmpty()) continue

                    if (currentChunk.length + sTrimmed.length + 1 <= maxChunkLength) {
                        if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                        currentChunk.append(sTrimmed)
                    } else {
                        if (currentChunk.isNotEmpty()) {
                            chunks.add(currentChunk.toString())
                            currentChunk = StringBuilder()
                        }
                        if (sTrimmed.length <= maxChunkLength) {
                            currentChunk.append(sTrimmed)
                        } else {
                            // If a single sentence is exceptionally long, split by commas or words
                            val words = sTrimmed.split(Regex("\\s+"))
                            for (word in words) {
                                if (currentChunk.length + word.length + 1 <= maxChunkLength) {
                                    if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                                    currentChunk.append(word)
                                } else {
                                    if (currentChunk.isNotEmpty()) {
                                        chunks.add(currentChunk.toString())
                                        currentChunk = StringBuilder()
                                    }
                                    currentChunk.append(word)
                                }
                            }
                        }
                    }
                }
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString())
                }
            }
        }

        return if (chunks.isNotEmpty()) chunks else listOf(trimmed)
    }

    suspend fun synthesizeSpeech(
        text: String,
        voice: Voice,
        style: VoiceStyle,
        speed: Float,
        outputFormat: String = "MP3",
        customBackendUrl: String? = null,
        customApiKey: String? = null,
        onProgress: (step: String, currentChunk: Int, totalChunks: Int) -> Unit
    ): Result<GeneratedAudioResult> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Text cannot be empty."))
        }

        val chunks = splitTextIntoChunks(trimmed)
        val totalChunks = chunks.size
        val chunkFiles = mutableListOf<File>()

        try {
            val hasInternet = isNetworkAvailable()
            val effectiveApiKey = if (!customApiKey.isNullOrBlank()) {
                customApiKey
            } else {
                try {
                    BuildConfig.GEMINI_API_KEY
                } catch (e: Throwable) {
                    ""
                }
            }

            val isMp3 = outputFormat.equals("MP3", ignoreCase = true)
            var engineUsed = if (isMp3) "Google Cloud Text-to-Speech (MP3)" else "Google Cloud Text-to-Speech (WAV)"

            for (index in chunks.indices) {
                val chunkText = chunks[index]
                val chunkNumber = index + 1

                onProgress("Generating section $chunkNumber of $totalChunks...", chunkNumber, totalChunks)

                val chunkExt = if (isMp3) "mp3" else "wav"
                val chunkFile = File(
                    AudioStorageManager.getAudioCacheDir(context),
                    "chunk_${System.currentTimeMillis()}_$index.$chunkExt"
                )

                var chunkSynthesized = false

                // 1. Try Custom Backend Proxy if configured
                if (hasInternet && !customBackendUrl.isNullOrBlank()) {
                    try {
                        val req = BackendTtsRequest(
                            text = chunkText,
                            language = voice.languageCode,
                            voice = voice.id,
                            style = style.name,
                            speed = speed
                        )
                        val resp = NetworkClient.geminiService.generateSpeechViaBackend(customBackendUrl, req)
                        if (resp.success && !resp.audioBase64.isNullOrBlank()) {
                            val audioBytes = Base64.decode(resp.audioBase64, Base64.DEFAULT)
                            val finalBytes = if (WavUtils.isWavFormat(audioBytes) || WavUtils.isMp3Format(audioBytes)) {
                                audioBytes
                            } else {
                                val sRate = WavUtils.parseSampleRate(resp.mimeType, 24000)
                                WavUtils.pcmToWav(audioBytes, sampleRate = sRate)
                            }
                            chunkFile.writeBytes(finalBytes)
                            chunkSynthesized = true
                            engineUsed = "Custom Backend Server"
                        }
                    } catch (e: Exception) {
                        Log.d("VoiceCraftAI", "Backend proxy attempt: ${e.message}")
                    }
                }

                // 2. Try Google Cloud Text-to-Speech API via TtsRepository
                if (!chunkSynthesized && hasInternet && effectiveApiKey.isNotBlank() && effectiveApiKey != "MY_GEMINI_API_KEY") {
                    val requestedEncoding = if (isMp3) "MP3" else "LINEAR16"
                    val cloudResult = ttsRepository.synthesizeSpeech(
                        text = chunkText,
                        languageCode = voice.languageCode,
                        voiceName = voice.googleCloudVoiceName,
                        ssmlGender = voice.ssmlGender,
                        speakingRate = (speed * style.rateMultiplier).toDouble(),
                        pitch = ((style.pitchMultiplier - 1.0f) * 10.0f).toDouble(),
                        audioEncoding = requestedEncoding,
                        apiKey = effectiveApiKey
                    )

                    cloudResult.onSuccess { rawAudioBytes ->
                        val finalBytes = if (WavUtils.isMp3Format(rawAudioBytes) || WavUtils.isWavFormat(rawAudioBytes)) {
                            rawAudioBytes
                        } else {
                            WavUtils.pcmToWav(rawAudioBytes, sampleRate = 24000)
                        }
                        chunkFile.writeBytes(finalBytes)
                        chunkSynthesized = true
                        engineUsed = "Google Cloud Text-to-Speech (${if (isMp3) "MP3" else "WAV"})"
                    }.onFailure { e ->
                        Log.d("VoiceCraftAI", "Google Cloud TTS repository attempt: ${e.message}")
                    }
                }

                // 3. Try Direct Google Gemini AI Audio (gemini-2.5-flash-preview-tts)
                if (!chunkSynthesized && hasInternet && effectiveApiKey.isNotBlank() && effectiveApiKey != "MY_GEMINI_API_KEY") {
                    try {
                        val promptWithStyle = "${style.promptInstruction}\nText to speak:\n$chunkText"
                        val geminiRequest = GeminiGenerateContentRequest(
                            contents = listOf(
                                GeminiContent(
                                    parts = listOf(GeminiPart(text = promptWithStyle))
                                )
                            ),
                            generationConfig = GeminiGenerationConfig(
                                responseModalities = listOf("AUDIO"),
                                speechConfig = GeminiSpeechConfig(
                                    voiceConfig = GeminiVoiceConfig(
                                        prebuiltVoiceConfig = GeminiPrebuiltVoiceConfig(
                                            voiceName = voice.geminiVoiceName
                                        )
                                    )
                                )
                            )
                        )

                        val response = NetworkClient.geminiService.generateSpeech(
                            model = "gemini-2.5-flash-preview-tts",
                            apiKey = effectiveApiKey,
                            request = geminiRequest
                        )

                        val inlineData = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData
                        if (inlineData != null && inlineData.data.isNotBlank()) {
                            val rawAudioBytes = Base64.decode(inlineData.data, Base64.DEFAULT)
                            val wavBytes = if (WavUtils.isWavFormat(rawAudioBytes) || WavUtils.isMp3Format(rawAudioBytes)) {
                                rawAudioBytes
                            } else {
                                val sRate = WavUtils.parseSampleRate(inlineData.mimeType, 24000)
                                WavUtils.pcmToWav(rawAudioBytes, sampleRate = sRate)
                            }
                            chunkFile.writeBytes(wavBytes)
                            chunkSynthesized = true
                            engineUsed = "Google Gemini AI Audio"
                        }
                    } catch (e: HttpException) {
                        Log.w("VoiceCraftAI", "Gemini HTTP ${e.code()}: Proceeding to device neural synthesis fallback.")
                    } catch (e: Exception) {
                        Log.w("VoiceCraftAI", "Gemini call exception: Proceeding to device neural synthesis fallback.")
                    }
                }

                // 3. Device High-Quality Neural Speech Synthesis (Guaranteed real audio, works offline)
                if (!chunkSynthesized) {
                    val localResult = androidTtsEngine.synthesizeToFile(
                        text = chunkText,
                        voice = voice,
                        style = style,
                        speed = speed,
                        outputFile = chunkFile
                    )
                    if (localResult.isSuccess) {
                        chunkSynthesized = true
                        engineUsed = "Device HD Speech Synthesis"
                    }
                }

                if (chunkSynthesized && chunkFile.exists() && chunkFile.length() > 0) {
                    chunkFiles.add(chunkFile)
                } else {
                    return@withContext Result.failure(
                        IllegalStateException("Unable to generate audio for section $chunkNumber. Please verify your script or connection.")
                    )
                }
            }

            onProgress("Processing and finalizing audio...", totalChunks, totalChunks)

            // Concatenate all chunks into final audio file with selected format
            val finalExt = if (isMp3) "mp3" else "wav"
            val finalFile = AudioStorageManager.createCacheAudioFile(context, finalExt)
            val concatSuccess = WavUtils.concatenateAudioFiles(chunkFiles, finalFile, targetFormat = outputFormat)

            if (!concatSuccess || !finalFile.exists() || finalFile.length() == 0L) {
                return@withContext Result.failure(IllegalStateException("Failed to assemble final audio file."))
            }

            // Clean up temporary chunk files
            chunkFiles.forEach { it.delete() }

            // Extract duration using MediaMetadataRetriever
            val durationMs = getAudioDuration(finalFile)

            Result.success(
                GeneratedAudioResult(
                    audioFile = finalFile,
                    durationMs = durationMs,
                    fileSizeBytes = finalFile.length(),
                    engineUsed = engineUsed
                )
            )
        } catch (e: Exception) {
            chunkFiles.forEach { it.delete() }
            Result.failure(e)
        }
    }

    private fun getAudioDuration(file: File): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun shutdown() {
        androidTtsEngine.shutdown()
    }
}
