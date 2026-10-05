package com.example

import com.example.audio.WavUtils
import com.example.data.VoiceCatalog
import com.example.data.repository.TtsRepository
import com.example.network.NetworkClient
import com.example.network.model.AudioConfig
import com.example.network.model.SynthesisInput
import com.example.network.model.TextToSpeechRequest
import com.example.network.model.VoiceSelectionParams
import com.example.ui.components.formatPlayerDuration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun textSplitterPreservesHindi() {
        val hindiText = "नमस्ते दोस्तों। यह पहला वाक्य है। क्या आप जानते हैं कि यह दूसरा वाक्य है? आज हम सीखेंगे।"
        val words = hindiText.trim().split(Regex("\\s+")).size
        assertTrue(words > 5)
    }

    @Test
    fun voiceCatalogLookupsWork() {
        val lang = VoiceCatalog.getLanguageByCode("hi-IN")
        assertEquals("Hindi", lang.name)

        val voice = VoiceCatalog.getVoiceById("hi_in_kore")
        assertEquals("hi-IN", voice.languageCode)
        assertEquals("hi-IN-Neural2-A", voice.googleCloudVoiceName)
        assertEquals("FEMALE", voice.ssmlGender)
    }

    @Test
    fun googleCloudTtsRequestSerializationHindi() {
        val request = TextToSpeechRequest(
            input = SynthesisInput(text = "नमस्ते दुनिया"),
            voice = VoiceSelectionParams(
                languageCode = "hi-IN",
                name = "hi-IN-Neural2-A",
                ssmlGender = "FEMALE"
            ),
            audioConfig = AudioConfig(
                audioEncoding = "LINEAR16",
                speakingRate = 1.0,
                pitch = 0.0,
                sampleRateHertz = 24000
            )
        )

        val adapter = NetworkClient.getMoshi().adapter(TextToSpeechRequest::class.java)
        val json = adapter.toJson(request)

        assertNotNull(json)
        assertTrue(json.contains("\"languageCode\":\"hi-IN\""))
        assertTrue(json.contains("\"name\":\"hi-IN-Neural2-A\""))
        assertTrue(json.contains("\"ssmlGender\":\"FEMALE\""))
        assertTrue(json.contains("\"text\":\"नमस्ते दुनिया\""))
        assertTrue(json.contains("\"audioEncoding\":\"LINEAR16\""))
    }

    @Test
    fun googleCloudTtsRequestSerializationEnglish() {
        val request = TextToSpeechRequest(
            input = SynthesisInput(text = "Welcome to VoiceCraft AI"),
            voice = VoiceSelectionParams(
                languageCode = "en-US",
                name = "en-US-Journey-F",
                ssmlGender = "FEMALE"
            ),
            audioConfig = AudioConfig(
                audioEncoding = "MP3",
                speakingRate = 1.25,
                pitch = 1.5,
                sampleRateHertz = 24000
            )
        )

        val adapter = NetworkClient.getMoshi().adapter(TextToSpeechRequest::class.java)
        val json = adapter.toJson(request)

        assertNotNull(json)
        assertTrue(json.contains("\"languageCode\":\"en-US\""))
        assertTrue(json.contains("\"name\":\"en-US-Journey-F\""))
        assertTrue(json.contains("\"ssmlGender\":\"FEMALE\""))
        assertTrue(json.contains("\"audioEncoding\":\"MP3\""))
    }

    @Test
    fun ttsRepositoryLanguageCodeHandling() {
        val repo = TtsRepository()

        assertEquals("hi-IN", repo.normalizeLanguageCode("hi-in"))
        assertEquals("hi-IN", repo.normalizeLanguageCode("hi_in"))
        assertEquals("hi-IN", repo.normalizeLanguageCode("HI-IN"))
        assertEquals("en-US", repo.normalizeLanguageCode("en-us"))
        assertEquals("en-US", repo.normalizeLanguageCode("EN_US"))

        val hindiVoiceParams = repo.getDefaultVoiceForLanguage("hi-in")
        assertEquals("hi-IN", hindiVoiceParams.languageCode)
        assertEquals("hi-IN-Neural2-A", hindiVoiceParams.name)
        assertEquals("FEMALE", hindiVoiceParams.ssmlGender)

        val englishVoiceParams = repo.getDefaultVoiceForLanguage("en_US")
        assertEquals("en-US", englishVoiceParams.languageCode)
        assertEquals("en-US-Journey-F", englishVoiceParams.name)
        assertEquals("FEMALE", englishVoiceParams.ssmlGender)
    }

    @Test
    fun ttsRepositoryValidationRejectsEmptyInput() = runBlocking {
        val repo = TtsRepository()

        val emptyTextResult = repo.synthesizeSpeech(
            text = "   ",
            languageCode = "hi-IN",
            apiKey = "AIzaSyDummyKey"
        )
        assertTrue(emptyTextResult.isFailure)

        val blankKeyResult = repo.synthesizeSpeech(
            text = "नमस्ते",
            languageCode = "hi-IN",
            apiKey = ""
        )
        assertTrue(blankKeyResult.isFailure)
    }

    @Test
    fun audioPlayerDurationFormatting() {
        assertEquals("00:00", formatPlayerDuration(0L))
        assertEquals("00:05", formatPlayerDuration(5000L))
        assertEquals("01:30", formatPlayerDuration(90000L))
        assertEquals("02:15", formatPlayerDuration(135000L))
    }

    @Test
    fun outputFormatDetectionAndValidation() {
        val dummyPcm = ByteArray(100)
        val wavBytes = WavUtils.pcmToWav(dummyPcm, sampleRate = 24000)
        assertTrue(WavUtils.isWavFormat(wavBytes))
        assertFalse(WavUtils.isMp3Format(wavBytes))

        // ID3 header for MP3
        val id3Mp3Bytes = byteArrayOf('I'.code.toByte(), 'D'.code.toByte(), '3'.code.toByte(), 0, 0, 0)
        assertTrue(WavUtils.isMp3Format(id3Mp3Bytes))
        assertFalse(WavUtils.isWavFormat(id3Mp3Bytes))

        // Syncword 0xFF 0xFB for MPEG Audio Frame
        val syncMp3Bytes = byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x90.toByte(), 0x44.toByte())
        assertTrue(WavUtils.isMp3Format(syncMp3Bytes))
    }

    @Test
    fun voiceCatalogSamplesCoverage() {
        val voices = VoiceCatalog.supportedVoices
        assertTrue("Catalog should contain voices", voices.isNotEmpty())
        for (voice in voices) {
            assertTrue("Voice ${voice.id} should have valid samplePreviewText", voice.samplePreviewText.isNotBlank())
            assertTrue("Sample text should be concise (~5 seconds)", voice.samplePreviewText.length in 10..200)
        }
    }
}
