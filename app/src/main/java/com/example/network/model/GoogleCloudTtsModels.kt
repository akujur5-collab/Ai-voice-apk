package com.example.network.model

import com.squareup.moshi.JsonClass

/**
 * Google Cloud Text-to-Speech Synthesis Request
 * 
 * Supports language codes (e.g. 'hi-IN', 'en-US') and voice selection parameters:
 * name, languageCode, and ssmlGender.
 */
@JsonClass(generateAdapter = true)
data class TextToSpeechRequest(
    val input: SynthesisInput,
    val voice: VoiceSelectionParams,
    val audioConfig: AudioConfig = AudioConfig()
)

@JsonClass(generateAdapter = true)
data class SynthesisInput(
    val text: String? = null,
    val ssml: String? = null
)

@JsonClass(generateAdapter = true)
data class VoiceSelectionParams(
    val languageCode: String, // e.g. "hi-IN", "en-US", "bn-IN", etc.
    val name: String? = null, // e.g. "hi-IN-Neural2-A", "en-US-Journey-F"
    val ssmlGender: String? = null // "SSML_VOICE_GENDER_UNSPECIFIED", "MALE", "FEMALE", "NEUTRAL"
)

@JsonClass(generateAdapter = true)
data class AudioConfig(
    val audioEncoding: String = "LINEAR16", // "LINEAR16" (WAV), "MP3", "OGG_OPUS"
    val speakingRate: Double? = 1.0, // 0.25 to 4.0
    val pitch: Double? = 0.0, // -20.0 to 20.0
    val volumeGainDb: Double? = 0.0,
    val sampleRateHertz: Int? = 24000
)

@JsonClass(generateAdapter = true)
data class TextToSpeechResponse(
    val audioContent: String? = null // Base64 encoded audio bytes
)
