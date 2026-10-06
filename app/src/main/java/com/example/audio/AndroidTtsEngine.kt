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
        initTts()
    }

    private fun initTts() {
        val appContext = context.applicationContext

        // Check if Google Speech Services (com.google.android.tts) is available for high-fidelity neural voices
        var hasGoogleTts = false
        try {
            val pm = appContext.packageManager
            pm.getPackageInfo("com.google.android.tts", 0)
            hasGoogleTts = true
        } catch (e: Exception) {
            hasGoogleTts = false
        }

        if (hasGoogleTts) {
            tts = TextToSpeech(appContext, { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    if (!initDeferred.isCompleted) initDeferred.complete(true)
                } else {
                    // Fallback to default engine if Google engine init fails
                    fallbackToDefaultEngine(appContext)
                }
            }, "com.google.android.tts")
        } else {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    if (!initDeferred.isCompleted) initDeferred.complete(true)
                } else {
                    isInitialized = false
                    if (!initDeferred.isCompleted) initDeferred.complete(false)
                }
            }
        }
    }

    private fun fallbackToDefaultEngine(appContext: Context) {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                if (!initDeferred.isCompleted) initDeferred.complete(true)
            } else {
                isInitialized = false
                if (!initDeferred.isCompleted) initDeferred.complete(false)
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
            ttsInstance.language = Locale.getDefault()
        }

        val isMale = voice.gender.equals("Male", ignoreCase = true)

        // 1. Calculate tailored human pitch based on voice profile
        // In human biology, adult male fundamental frequency is ~110-130Hz, female is ~210-240Hz.
        // A pitch multiplier of ~0.72f-0.78f brings vocal resonance into natural deep male range.
        val basePitch = when (voice.id) {
            "hi_in_charon", "en_us_charon", "en_gb_charon" -> 0.70f // Deep Narrator Male (rich baritone)
            "hi_in_fenrir", "en_us_fenrir", "en_gb_fenrir" -> 0.75f // Bold & Motivational Male (firm resonance)
            "hi_in_puck", "en_us_puck" -> 0.82f // Youthful Energetic Male (tenor)
            "hi_in_aoede", "en_us_aoede" -> 1.14f // Melodic Soft Female
            "hi_in_kore", "en_us_kore" -> 1.03f // Warm & Expressive Female
            else -> if (isMale) 0.74f else 1.05f
        }

        // 2. Style-adjusted speaking rate for natural, non-robotic human phrasing
        val styleRateFactor = when (style) {
            VoiceStyle.NARRATOR -> 0.92f // deliberate documentary pace with emphasis
            VoiceStyle.STORYTELLING -> 0.94f // dramatic storyteller pace
            VoiceStyle.DEVOTIONAL, VoiceStyle.CALM -> 0.88f // peaceful sacred cadence
            VoiceStyle.NEWS, VoiceStyle.PROFESSIONAL -> 1.00f // broadcast cadence
            VoiceStyle.ENERGETIC, VoiceStyle.ADVERTISEMENT -> 1.06f // punchy commercial cadence
            VoiceStyle.EDUCATIONAL -> 0.95f // clear teaching cadence
            else -> 0.97f
        }

        val finalRate = (speed * styleRateFactor).coerceIn(0.5f, 2.0f)
        val finalPitch = (basePitch * style.pitchMultiplier).coerceIn(0.55f, 1.45f)

        ttsInstance.setSpeechRate(finalRate)
        ttsInstance.setPitch(finalPitch)

        // 3. Match male vs female voice from available TTS voice packs
        try {
            val systemVoices = ttsInstance.voices
            if (!systemVoices.isNullOrEmpty()) {
                val langMatchingVoices = systemVoices.filter { sysVoice ->
                    sysVoice.locale.language.equals(locale.language, ignoreCase = true)
                }

                if (langMatchingVoices.isNotEmpty()) {
                    val targetVoice = if (isMale) {
                        // Priority 1: Match features tag
                        langMatchingVoices.firstOrNull { v ->
                            val feat = try { v.features ?: emptySet() } catch (e: Exception) { emptySet() }
                            feat.any { it.contains("male", ignoreCase = true) }
                        }
                        // Priority 2: Match known Google TTS and OEM male voice identifier patterns
                        ?: langMatchingVoices.firstOrNull { v ->
                            val n = v.name.lowercase()
                            n.contains("-hid-") || n.contains("-hif-") || // Hindi male (Google TTS)
                            n.contains("-tpd-") || n.contains("-iol-") || n.contains("-iom-") || // US male (Google TTS)
                            n.contains("-rjs-") || n.contains("-fis-") || // UK male (Google TTS)
                            n.contains("-bnd-") || n.contains("-mrd-") || n.contains("-tad-") || n.contains("-ted-") || n.contains("-gud-") || // Indic male
                            n.contains("male") || n.contains("-d-") || n.contains("-f-") || n.contains("-b-") ||
                            n.contains("man") || n.contains("boy") || n.contains("m0")
                        }
                        // Priority 3: If multiple voices exist, use second voice (differentiates from first default female)
                        ?: if (langMatchingVoices.size > 1) langMatchingVoices[1] else null
                    } else {
                        // Female voice selection
                        langMatchingVoices.firstOrNull { v ->
                            val feat = try { v.features ?: emptySet() } catch (e: Exception) { emptySet() }
                            feat.any { it.contains("female", ignoreCase = true) }
                        }
                        ?: langMatchingVoices.firstOrNull { v ->
                            val n = v.name.lowercase()
                            n.contains("-hia-") || n.contains("-hic-") || n.contains("-hie-") || // Hindi female
                            n.contains("-sfg-") || n.contains("-tpc-") || // US female
                            n.contains("-gba-") || n.contains("-bna-") || n.contains("-mra-") || // Indic female
                            n.contains("female") || n.contains("-a-") || n.contains("-c-") || n.contains("-e-") ||
                            n.contains("woman") || n.contains("girl") || n.contains("f0")
                        }
                        ?: langMatchingVoices.firstOrNull()
                    }

                    if (targetVoice != null) {
                        ttsInstance.voice = targetVoice
                    }
                }
            }
        } catch (e: Exception) {
            // Voices inspection optional on custom ROMs
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
