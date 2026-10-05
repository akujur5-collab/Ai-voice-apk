package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioStorageManager
import com.example.audio.AudioSynthesisEngine
import com.example.audio.PlayerState
import com.example.data.VoiceCatalog
import com.example.data.database.AppDatabase
import com.example.data.database.TtsProjectEntity
import com.example.data.database.UserSettingsEntity
import com.example.data.model.Language
import com.example.data.model.SampleScript
import com.example.data.model.ShortsPreset
import com.example.data.model.Voice
import com.example.data.model.VoiceStyle
import com.example.data.repository.TtsRepository
import com.example.data.repository.VoiceCraftRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface GenerationState {
    object Idle : GenerationState
    object Validating : GenerationState
    data class Generating(val progressText: String, val currentChunk: Int, val totalChunks: Int) : GenerationState
    object Processing : GenerationState
    data class Success(val project: TtsProjectEntity, val engineUsed: String) : GenerationState
    data class Error(val message: String) : GenerationState
}

data class HomeUiState(
    val scriptText: String = "",
    val characterCount: Int = 0,
    val wordCount: Int = 0,
    val selectedLanguage: Language = VoiceCatalog.supportedLanguages.first(),
    val selectedVoice: Voice = VoiceCatalog.supportedVoices.first(),
    val selectedStyle: VoiceStyle = VoiceStyle.NATURAL,
    val speed: Float = 1.0f,
    val isYouTubeMode: Boolean = false,
    val youtubeHook: String = "",
    val youtubeMain: String = "",
    val youtubeCta: String = "",
    val selectedShortsPreset: ShortsPreset? = null,
    val previewingVoiceId: String? = null,
    val isPreviewLoading: Boolean = false,
    val isPreviewPlaying: Boolean = false,
    val generationState: GenerationState = GenerationState.Idle,
    val isFavoriteVoice: Boolean = false,
    val outputFormat: String = "MP3",
    val downloadNotificationMessage: String? = null,
    val userSettings: UserSettingsEntity = UserSettingsEntity()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    val repository = VoiceCraftRepository(database)
    val ttsRepository: TtsRepository = TtsRepository()

    private val synthesisEngine = AudioSynthesisEngine(context, ttsRepository)
    val audioPlayerManager = AudioPlayerManager(context)
    private var sampleTimeoutJob: Job? = null

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<PlayerState> = audioPlayerManager.playerState

    val favoriteVoiceIds: StateFlow<List<String>> = repository.favoriteVoiceIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val lang = VoiceCatalog.getLanguageByCode(settings.defaultLanguageCode)
            val voice = VoiceCatalog.getVoiceById(settings.defaultVoiceId)
            val style = try {
                VoiceStyle.valueOf(settings.defaultStyle)
            } catch (e: Exception) {
                VoiceStyle.NATURAL
            }

            val isFav = repository.isVoiceFavorite(voice.id)

            _uiState.value = _uiState.value.copy(
                selectedLanguage = lang,
                selectedVoice = voice,
                selectedStyle = style,
                speed = settings.defaultSpeed,
                isFavoriteVoice = isFav,
                outputFormat = settings.outputFormat,
                userSettings = settings
            )
        }

        viewModelScope.launch {
            audioPlayerManager.playerState.collect { pState ->
                if (!pState.isPlaying && _uiState.value.previewingVoiceId != null) {
                    _uiState.value = _uiState.value.copy(
                        previewingVoiceId = null,
                        isPreviewPlaying = false
                    )
                }
            }
        }
    }

    fun onOutputFormatSelected(format: String) {
        val normalized = if (format.equals("WAV", ignoreCase = true)) "WAV" else "MP3"
        _uiState.value = _uiState.value.copy(outputFormat = normalized)
    }

    fun onScriptTextChanged(newText: String) {
        val words = if (newText.isBlank()) 0 else newText.trim().split(Regex("\\s+")).size
        _uiState.value = _uiState.value.copy(
            scriptText = newText,
            characterCount = newText.length,
            wordCount = words
        )
    }

    fun onYouTubeModeToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isYouTubeMode = enabled)
        if (enabled) {
            updateScriptFromYouTubeParts()
        }
    }

    fun onYouTubePartsChanged(hook: String, main: String, cta: String) {
        _uiState.value = _uiState.value.copy(
            youtubeHook = hook,
            youtubeMain = main,
            youtubeCta = cta
        )
        updateScriptFromYouTubeParts()
    }

    private fun updateScriptFromYouTubeParts() {
        val parts = listOf(
            _uiState.value.youtubeHook.trim(),
            _uiState.value.youtubeMain.trim(),
            _uiState.value.youtubeCta.trim()
        ).filter { it.isNotEmpty() }

        val combined = parts.joinToString("\n\n")
        onScriptTextChanged(combined)
    }

    fun onShortsPresetSelected(preset: ShortsPreset?) {
        _uiState.value = _uiState.value.copy(
            selectedShortsPreset = if (_uiState.value.selectedShortsPreset == preset) null else preset
        )
    }

    fun onLanguageSelected(language: Language) {
        val availableVoices = VoiceCatalog.getVoicesForLanguage(language.code)
        val newVoice = availableVoices.firstOrNull() ?: VoiceCatalog.supportedVoices.first()

        viewModelScope.launch {
            val isFav = repository.isVoiceFavorite(newVoice.id)
            _uiState.value = _uiState.value.copy(
                selectedLanguage = language,
                selectedVoice = newVoice,
                isFavoriteVoice = isFav
            )
        }
    }

    fun onVoiceSelected(voice: Voice) {
        val lang = VoiceCatalog.getLanguageByCode(voice.languageCode)
        viewModelScope.launch {
            val isFav = repository.isVoiceFavorite(voice.id)
            _uiState.value = _uiState.value.copy(
                selectedVoice = voice,
                selectedLanguage = lang,
                isFavoriteVoice = isFav
            )
        }
    }

    fun onStyleSelected(style: VoiceStyle) {
        _uiState.value = _uiState.value.copy(selectedStyle = style)
    }

    fun onSpeedChanged(newSpeed: Float) {
        _uiState.value = _uiState.value.copy(speed = newSpeed)
        audioPlayerManager.setPlaybackSpeed(newSpeed)
    }

    fun toggleFavoriteCurrentVoice() {
        val voice = _uiState.value.selectedVoice
        viewModelScope.launch {
            val nowFav = repository.toggleFavoriteVoice(voice.id, voice.languageCode)
            _uiState.value = _uiState.value.copy(isFavoriteVoice = nowFav)
        }
    }

    fun previewVoice(voice: Voice) {
        if (_uiState.value.previewingVoiceId == voice.id && audioPlayerManager.playerState.value.isPlaying) {
            stopVoicePreview()
            return
        }

        sampleTimeoutJob?.cancel()
        audioPlayerManager.stop()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                previewingVoiceId = voice.id,
                isPreviewLoading = true,
                isPreviewPlaying = true
            )

            val formatExt = if (_uiState.value.outputFormat.equals("MP3", ignoreCase = true)) "mp3" else "wav"
            val sampleCacheFile = File(
                AudioStorageManager.getAudioCacheDir(context),
                "narrator_sample_${voice.id}.$formatExt"
            )

            val audioPathToPlay = if (sampleCacheFile.exists() && sampleCacheFile.length() > 0) {
                sampleCacheFile.absolutePath
            } else {
                val previewText = voice.samplePreviewText
                val result = synthesisEngine.synthesizeSpeech(
                    text = previewText,
                    voice = voice,
                    style = _uiState.value.selectedStyle,
                    speed = _uiState.value.speed,
                    outputFormat = _uiState.value.outputFormat,
                    customBackendUrl = _uiState.value.userSettings.customBackendUrl.takeIf { it.isNotBlank() },
                    customApiKey = _uiState.value.userSettings.customApiKey.takeIf { it.isNotBlank() },
                    onProgress = { _, _, _ -> }
                )

                if (result.isSuccess) {
                    val file = result.getOrThrow().audioFile
                    file.copyTo(sampleCacheFile, overwrite = true)
                    sampleCacheFile.absolutePath
                } else {
                    _uiState.value = _uiState.value.copy(
                        previewingVoiceId = null,
                        isPreviewLoading = false,
                        isPreviewPlaying = false,
                        downloadNotificationMessage = "Could not fetch sample: ${result.exceptionOrNull()?.message}"
                    )
                    null
                }
            }

            if (audioPathToPlay != null) {
                _uiState.value = _uiState.value.copy(
                    isPreviewLoading = false,
                    previewingVoiceId = voice.id,
                    isPreviewPlaying = true
                )
                audioPlayerManager.loadAudio(audioPathToPlay, autoPlay = true)

                // Limit playback to exactly 5 seconds
                sampleTimeoutJob = viewModelScope.launch {
                    delay(5000L)
                    if (_uiState.value.previewingVoiceId == voice.id) {
                        audioPlayerManager.stop()
                        _uiState.value = _uiState.value.copy(
                            previewingVoiceId = null,
                            isPreviewPlaying = false
                        )
                    }
                }
            }
        }
    }

    fun stopVoicePreview() {
        sampleTimeoutJob?.cancel()
        audioPlayerManager.stop()
        _uiState.value = _uiState.value.copy(
            previewingVoiceId = null,
            isPreviewPlaying = false
        )
    }

    fun generateSpeech() {
        val state = _uiState.value
        val text = state.scriptText.trim()

        if (text.isEmpty()) {
            _uiState.value = state.copy(
                generationState = GenerationState.Error("Please enter or paste your script text before generating.")
            )
            return
        }

        if (state.generationState is GenerationState.Generating || state.generationState is GenerationState.Processing) {
            return // prevent duplicate clicks
        }

        viewModelScope.launch {
            _uiState.value = state.copy(generationState = GenerationState.Validating)

            val synthResult = synthesisEngine.synthesizeSpeech(
                text = text,
                voice = state.selectedVoice,
                style = state.selectedStyle,
                speed = state.speed,
                outputFormat = state.outputFormat,
                customBackendUrl = state.userSettings.customBackendUrl.takeIf { it.isNotBlank() },
                customApiKey = state.userSettings.customApiKey.takeIf { it.isNotBlank() },
                onProgress = { progressText, current, total ->
                    _uiState.value = _uiState.value.copy(
                        generationState = GenerationState.Generating(progressText, current, total)
                    )
                }
            )

            synthResult.onSuccess { result ->
                _uiState.value = _uiState.value.copy(generationState = GenerationState.Processing)

                // Derive title from first words
                val cleanTitle = text.lines().firstOrNull { it.isNotBlank() }?.take(40) ?: "VoiceCraft Project"

                val project = TtsProjectEntity(
                    title = cleanTitle,
                    text = text,
                    languageCode = state.selectedVoice.languageCode,
                    languageName = state.selectedLanguage.name,
                    voiceId = state.selectedVoice.id,
                    voiceName = state.selectedVoice.name,
                    style = state.selectedStyle.displayName,
                    speed = state.speed,
                    durationMs = result.durationMs,
                    audioFilePath = result.audioFile.absolutePath,
                    fileSizeBytes = result.fileSizeBytes
                )

                var savedProject = project
                if (state.userSettings.autoSaveHistory) {
                    val id = repository.saveProject(project)
                    savedProject = project.copy(id = id)
                }

                audioPlayerManager.loadAudio(result.audioFile.absolutePath, autoPlay = true)

                _uiState.value = _uiState.value.copy(
                    generationState = GenerationState.Success(savedProject, result.engineUsed)
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    generationState = GenerationState.Error(
                        error.message ?: "Speech generation encountered an issue. Please try again."
                    )
                )
            }
        }
    }

    fun saveCurrentProject() {
        val genState = _uiState.value.generationState
        if (genState is GenerationState.Success) {
            viewModelScope.launch {
                repository.saveProject(genState.project)
                _uiState.value = _uiState.value.copy(
                    downloadNotificationMessage = "Project saved to History!"
                )
            }
        }
    }

    fun downloadCurrentAudio(): Result<Uri>? {
        val genState = _uiState.value.generationState
        if (genState is GenerationState.Success) {
            val file = File(genState.project.audioFilePath)
            val result = AudioStorageManager.exportToPublicStorage(
                context,
                file,
                genState.project.title
            )
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    downloadNotificationMessage = "Audio saved to Music/VoiceCraft on your device!"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    downloadNotificationMessage = "Failed to download: ${err.message}"
                )
            }
            return result
        }
        return null
    }

    fun createShareIntent(): Intent? {
        val genState = _uiState.value.generationState
        if (genState is GenerationState.Success) {
            val file = File(genState.project.audioFilePath)
            if (file.exists()) {
                return AudioStorageManager.createShareIntent(context, file, genState.project.title)
            }
        }
        return null
    }

    fun clearNotificationMessage() {
        _uiState.value = _uiState.value.copy(downloadNotificationMessage = null)
    }

    // Text tools
    fun pasteFromClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val pastedText = clip.getItemAt(0).text?.toString() ?: ""
            if (pastedText.isNotEmpty()) {
                val current = _uiState.value.scriptText
                val newText = if (current.isEmpty()) pastedText else "$current\n$pastedText"
                onScriptTextChanged(newText)
            }
        }
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("VoiceCraft Script", _uiState.value.scriptText)
        clipboard?.setPrimaryClip(clip)
        _uiState.value = _uiState.value.copy(
            downloadNotificationMessage = "Script copied to clipboard!"
        )
    }

    fun clearText() {
        onScriptTextChanged("")
        _uiState.value = _uiState.value.copy(
            youtubeHook = "",
            youtubeMain = "",
            youtubeCta = "",
            generationState = GenerationState.Idle
        )
    }

    fun applySampleScript(sample: SampleScript) {
        val lang = VoiceCatalog.getLanguageByCode(sample.languageCode)
        onLanguageSelected(lang)
        onScriptTextChanged(sample.text)
    }

    fun cleanText() {
        // Removes extra whitespace and trims each line without altering characters/meaning
        val cleaned = _uiState.value.scriptText
            .lines()
            .map { it.trim().replace(Regex("[ \\t]+"), " ") }
            .filter { it.isNotEmpty() }
            .joinToString("\n\n")
        onScriptTextChanged(cleaned)
    }

    fun removeExtraSpaces() {
        val cleaned = _uiState.value.scriptText.replace(Regex("[ \\t]+"), " ")
        onScriptTextChanged(cleaned)
    }

    fun removeEmptyLines() {
        val cleaned = _uiState.value.scriptText
            .lines()
            .filter { it.trim().isNotEmpty() }
            .joinToString("\n")
        onScriptTextChanged(cleaned)
    }

    fun formatParagraphs() {
        val paragraphs = _uiState.value.scriptText
            .split(Regex("(?<=[.!?।|])\\s+"))
            .chunked(3)
            .joinToString("\n\n") { it.joinToString(" ") }
        onScriptTextChanged(paragraphs)
    }

    fun loadProjectIntoEditor(project: TtsProjectEntity) {
        val lang = VoiceCatalog.getLanguageByCode(project.languageCode)
        val voice = VoiceCatalog.getVoiceById(project.voiceId)
        val style = try {
            VoiceStyle.valueOf(project.style.uppercase())
        } catch (e: Exception) {
            VoiceStyle.NATURAL
        }

        onScriptTextChanged(project.text)
        _uiState.value = _uiState.value.copy(
            selectedLanguage = lang,
            selectedVoice = voice,
            selectedStyle = style,
            speed = project.speed,
            generationState = GenerationState.Success(project, "Saved Project")
        )

        if (File(project.audioFilePath).exists()) {
            audioPlayerManager.loadAudio(project.audioFilePath, autoPlay = false)
        }
    }

    /**
     * Directly calls Google Cloud Text-to-Speech service via TtsRepository.
     */
    suspend fun synthesizeWithGoogleCloudTts(
        text: String,
        languageCode: String,
        voiceName: String? = null,
        ssmlGender: String? = null,
        speakingRate: Double = 1.0,
        pitch: Double = 0.0,
        apiKey: String
    ): Result<ByteArray> {
        return ttsRepository.synthesizeSpeech(
            text = text,
            languageCode = languageCode,
            voiceName = voiceName,
            ssmlGender = ssmlGender,
            speakingRate = speakingRate,
            pitch = pitch,
            apiKey = apiKey
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        synthesisEngine.shutdown()
    }
}
