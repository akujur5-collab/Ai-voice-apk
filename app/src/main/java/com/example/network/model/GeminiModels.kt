package com.example.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    val responseModalities: List<String>? = listOf("AUDIO"),
    val speechConfig: GeminiSpeechConfig? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiSpeechConfig(
    val voiceConfig: GeminiVoiceConfig
)

@JsonClass(generateAdapter = true)
data class GeminiVoiceConfig(
    val prebuiltVoiceConfig: GeminiPrebuiltVoiceConfig
)

@JsonClass(generateAdapter = true)
data class GeminiPrebuiltVoiceConfig(
    val voiceName: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null,
    val error: GeminiApiError? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiApiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@JsonClass(generateAdapter = true)
data class BackendTtsRequest(
    val text: String,
    val language: String,
    val voice: String,
    val style: String,
    val speed: Float
)

@JsonClass(generateAdapter = true)
data class BackendTtsResponse(
    val success: Boolean = true,
    val audioBase64: String? = null,
    val audioUrl: String? = null,
    val mimeType: String? = null,
    val error: String? = null
)
