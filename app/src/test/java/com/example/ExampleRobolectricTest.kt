package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.WavUtils
import com.example.data.VoiceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readAppNameFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VoiceCraft AI", appName)
    }

    @Test
    fun voiceCatalogHasLanguagesAndVoices() {
        val languages = VoiceCatalog.supportedLanguages
        assertTrue(languages.isNotEmpty())
        assertTrue(languages.any { it.code == "hi-IN" })
        assertTrue(languages.any { it.code == "en-US" })

        val hindiVoices = VoiceCatalog.getVoicesForLanguage("hi-IN")
        assertTrue(hindiVoices.isNotEmpty())

        val englishVoices = VoiceCatalog.getVoicesForLanguage("en-US")
        assertTrue(englishVoices.isNotEmpty())
    }

    @Test
    fun wavUtilsGeneratesValidHeader() {
        val dummyPcm = ByteArray(4800) { 0 }
        val wavBytes = WavUtils.pcmToWav(dummyPcm, sampleRate = 24000, channels = 1, bitsPerSample = 16)
        assertEquals(4844, wavBytes.size)
        assertTrue(WavUtils.isWavFormat(wavBytes))
    }
}
