package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.Voice
import com.example.data.model.VoiceStyle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

class AndroidTtsEngine(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val initDeferred = CompletableDeferred<Boolean>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                initDeferred.complete(true)
            } else {
                isInitialized = false
                initDeferred.complete(false)
            }
        }
    }

    suspend fun awaitInitialization(): Boolean {
        return if (isInitialized) true else initDeferred.await()
    }

    suspend fun synthesizeToFile(
        text: String,
        voice: Voice,
        style: VoiceStyle,
        speed: Float,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        val ready = awaitInitialization()
        if (!ready || tts == null) {
            return@withContext Result.failure(IllegalStateException("TTS engine could not be initialized on device"))
        }

        val ttsInstance = tts!!
        val locale = Locale.forLanguageTag(voice.languageCode)

        val available = ttsInstance.isLanguageAvailable(locale)
        if (available >= TextToSpeech.LANG_AVAILABLE) {
            ttsInstance.language = locale
        } else {
            // fallback to default locale or English if exact subtag not installed
            ttsInstance.language = Locale.getDefault()
        }

        // Apply rate and pitch based on speed and style
        val finalRate = (speed * style.rateMultiplier).coerceIn(0.5f, 2.0f)
        val finalPitch = (style.pitchMultiplier).coerceIn(0.7f, 1.4f)
        ttsInstance.setSpeechRate(finalRate)
        ttsInstance.setPitch(finalPitch)

        // Try to match voice name if available in TTS voices
        try {
            val systemVoices = ttsInstance.voices
            if (!systemVoices.isNullOrEmpty()) {
                val matched = systemVoices.firstOrNull { sysVoice ->
                    sysVoice.locale.language.equals(locale.language, ignoreCase = true) &&
                            (voice.gender.equals("Female", ignoreCase = true) == sysVoice.name.contains("female", ignoreCase = true) ||
                             voice.gender.equals("Male", ignoreCase = true) == sysVoice.name.contains("male", ignoreCase = true))
                }
                if (matched != null) {
                    ttsInstance.voice = matched
                }
            }
        } catch (e: Exception) {
            // Voices inspection optional on some custom ROMs
        }

        val utteranceId = UUID.randomUUID().toString()
        val synthesisDeferred = CompletableDeferred<Boolean>()

        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    synthesisDeferred.complete(true)
                }
            }

            override fun onError(id: String?) {
                if (id == utteranceId) {
                    synthesisDeferred.complete(false)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?, errorCode: Int) {
                if (id == utteranceId) {
                    synthesisDeferred.complete(false)
                }
            }
        })

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        val result = ttsInstance.synthesizeToFile(text, params, outputFile, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            return@withContext Result.failure(IllegalStateException("Failed to schedule synthesis with TTS engine"))
        }

        val success = synthesisDeferred.await()
        if (success && outputFile.exists() && outputFile.length() > 0) {
            Result.success(outputFile)
        } else {
            Result.failure(IllegalStateException("TTS output file was not generated or is empty"))
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            // ignore
        }
    }
}
