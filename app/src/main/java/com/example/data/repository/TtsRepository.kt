package com.example.data.repository

import android.util.Base64
import com.example.network.GoogleCloudTtsApiService
import com.example.network.NetworkClient
import com.example.network.model.AudioConfig
import com.example.network.model.SynthesisInput
import com.example.network.model.TextToSpeechRequest
import com.example.network.model.VoiceSelectionParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository to manage API calls to the Google Cloud Text-to-Speech service.
 * Handles language codes (e.g., 'hi-IN', 'en-US') and voice configurations,
 * providing clean synthesis operations to ViewModels and synthesis engines.
 */
class TtsRepository(
    private val ttsApiService: GoogleCloudTtsApiService = NetworkClient.googleCloudTtsService
) {

    /**
     * Synthesizes text into audio bytes using the Google Cloud Text-to-Speech API.
     *
     * @param text The plain text to convert into speech.
     * @param languageCode BCP-47 language tag (e.g., 'hi-IN', 'en-US').
     * @param voiceName Optional specific Google Cloud voice identifier (e.g., 'hi-IN-Neural2-A').
     * @param ssmlGender Optional voice gender ('MALE', 'FEMALE', 'NEUTRAL').
     * @param speakingRate Speaking rate (0.25 to 4.0, default 1.0).
     * @param pitch Speaking pitch (-20.0 to 20.0, default 0.0).
     * @param audioEncoding Audio encoding format ('LINEAR16', 'MP3', 'OGG_OPUS').
     * @param apiKey The Google API Key with Text-to-Speech API enabled.
     * @return Result containing the raw audio bytes or error.
     */
    suspend fun synthesizeSpeech(
        text: String,
        languageCode: String,
        voiceName: String? = null,
        ssmlGender: String? = null,
        speakingRate: Double = 1.0,
        pitch: Double = 0.0,
        audioEncoding: String = "LINEAR16",
        apiKey: String
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Text cannot be empty"))
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalArgumentException("Valid API key is required"))
        }

        // Validate and normalize language code (e.g., 'hi-in' -> 'hi-IN', 'en-us' -> 'en-US')
        val normalizedLangCode = normalizeLanguageCode(languageCode)

        try {
            val request = TextToSpeechRequest(
                input = SynthesisInput(text = trimmed),
                voice = VoiceSelectionParams(
                    languageCode = normalizedLangCode,
                    name = voiceName?.takeIf { it.isNotBlank() },
                    ssmlGender = ssmlGender?.takeIf { it.isNotBlank() }
                ),
                audioConfig = AudioConfig(
                    audioEncoding = audioEncoding,
                    speakingRate = speakingRate.coerceIn(0.25, 4.0),
                    pitch = pitch.coerceIn(-20.0, 20.0),
                    sampleRateHertz = 24000
                )
            )

            val response = ttsApiService.synthesizeSpeech(
                apiKey = apiKey,
                request = request
            )

            val audioContent = response.audioContent
            if (!audioContent.isNullOrBlank()) {
                val bytes = Base64.decode(audioContent, Base64.DEFAULT)
                Result.success(bytes)
            } else {
                Result.failure(IllegalStateException("No audio content returned by Google Cloud TTS"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Synthesizes speech and writes the resulting audio bytes directly into a target file.
     */
    suspend fun synthesizeToFile(
        text: String,
        languageCode: String,
        voiceName: String? = null,
        ssmlGender: String? = null,
        speakingRate: Double = 1.0,
        pitch: Double = 0.0,
        audioEncoding: String = "LINEAR16",
        outputFile: File,
        apiKey: String
    ): Result<File> = withContext(Dispatchers.IO) {
        synthesizeSpeech(
            text = text,
            languageCode = languageCode,
            voiceName = voiceName,
            ssmlGender = ssmlGender,
            speakingRate = speakingRate,
            pitch = pitch,
            audioEncoding = audioEncoding,
            apiKey = apiKey
        ).map { bytes ->
            outputFile.writeBytes(bytes)
            outputFile
        }
    }

    /**
     * Normalizes language codes to standard BCP-47 tag format (e.g., 'hi-in' -> 'hi-IN', 'en_us' -> 'en-US').
     */
    fun normalizeLanguageCode(code: String): String {
        val trimmed = code.trim()
        val parts = trimmed.split("-", "_")
        return if (parts.size >= 2) {
            "${parts[0].lowercase()}-${parts[1].uppercase()}"
        } else {
            trimmed.lowercase()
        }
    }

    /**
     * Helper to construct voice selection parameters.
     */
    fun createVoiceSelectionParams(
        languageCode: String,
        voiceName: String? = null,
        ssmlGender: String? = null
    ): VoiceSelectionParams {
        return VoiceSelectionParams(
            languageCode = normalizeLanguageCode(languageCode),
            name = voiceName?.takeIf { it.isNotBlank() },
            ssmlGender = ssmlGender?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Returns default voice parameters for common languages like Hindi ('hi-IN') and English ('en-US').
     */
    fun getDefaultVoiceForLanguage(languageCode: String): VoiceSelectionParams {
        val normalized = normalizeLanguageCode(languageCode)
        return when (normalized) {
            "hi-IN" -> VoiceSelectionParams(
                languageCode = "hi-IN",
                name = "hi-IN-Neural2-A",
                ssmlGender = "FEMALE"
            )
            "en-US" -> VoiceSelectionParams(
                languageCode = "en-US",
                name = "en-US-Journey-F",
                ssmlGender = "FEMALE"
            )
            "en-GB" -> VoiceSelectionParams(
                languageCode = "en-GB",
                name = "en-GB-Neural2-A",
                ssmlGender = "FEMALE"
            )
            "bn-IN" -> VoiceSelectionParams(
                languageCode = "bn-IN",
                name = "bn-IN-Wavenet-A",
                ssmlGender = "FEMALE"
            )
            "mr-IN" -> VoiceSelectionParams(
                languageCode = "mr-IN",
                name = "mr-IN-Wavenet-A",
                ssmlGender = "FEMALE"
            )
            "ta-IN" -> VoiceSelectionParams(
                languageCode = "ta-IN",
                name = "ta-IN-Wavenet-A",
                ssmlGender = "FEMALE"
            )
            "te-IN" -> VoiceSelectionParams(
                languageCode = "te-IN",
                name = "te-IN-Standard-A",
                ssmlGender = "FEMALE"
            )
            else -> VoiceSelectionParams(
                languageCode = normalized,
                name = null,
                ssmlGender = null
            )
        }
    }
}
