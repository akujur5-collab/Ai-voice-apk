package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioSynthesisEngine
import com.example.audio.PlayerState
import com.example.data.VoiceCatalog
import com.example.data.database.AppDatabase
import com.example.data.model.Voice
import com.example.data.model.VoiceStyle
import com.example.data.repository.TtsRepository
import com.example.data.repository.VoiceCraftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class VoicesFilterState(
    val searchQuery: String = "",
    val selectedLanguageCode: String? = null,
    val selectedGender: String = "All", // "All", "Female", "Male"
    val onlyFavorites: Boolean = false
)

class VoicesViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    val repository = VoiceCraftRepository(database)
    val ttsRepository: TtsRepository = TtsRepository()

    private val synthesisEngine = AudioSynthesisEngine(context, ttsRepository)
    val audioPlayerManager = AudioPlayerManager(context)
    val playerState: StateFlow<PlayerState> = audioPlayerManager.playerState

    private val _filterState = MutableStateFlow(VoicesFilterState())
    val filterState: StateFlow<VoicesFilterState> = _filterState.asStateFlow()

    private val _previewingVoiceId = MutableStateFlow<String?>(null)
    val previewingVoiceId: StateFlow<String?> = _previewingVoiceId.asStateFlow()

    val favoriteVoiceIds: StateFlow<List<String>> = repository.favoriteVoiceIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredVoices: StateFlow<List<Voice>> = combine(
        _filterState,
        favoriteVoiceIds
    ) { filter, favIds ->
        VoiceCatalog.supportedVoices.filter { voice ->
            val matchesQuery = filter.searchQuery.isBlank() ||
                    voice.name.contains(filter.searchQuery, ignoreCase = true) ||
                    voice.description.contains(filter.searchQuery, ignoreCase = true)

            val matchesLang = filter.selectedLanguageCode == null ||
                    voice.languageCode == filter.selectedLanguageCode

            val matchesGender = filter.selectedGender == "All" ||
                    voice.gender.equals(filter.selectedGender, ignoreCase = true)

            val matchesFav = !filter.onlyFavorites || favIds.contains(voice.id)

            matchesQuery && matchesLang && matchesGender && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VoiceCatalog.supportedVoices)

    fun onSearchQueryChanged(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun onLanguageFilterSelected(langCode: String?) {
        _filterState.value = _filterState.value.copy(selectedLanguageCode = langCode)
    }

    fun onGenderFilterSelected(gender: String) {
        _filterState.value = _filterState.value.copy(selectedGender = gender)
    }

    fun toggleFavoritesOnly() {
        _filterState.value = _filterState.value.copy(onlyFavorites = !_filterState.value.onlyFavorites)
    }

    fun toggleFavorite(voice: Voice) {
        viewModelScope.launch {
            repository.toggleFavoriteVoice(voice.id, voice.languageCode)
        }
    }

    fun previewVoice(voice: Voice) {
        viewModelScope.launch {
            _previewingVoiceId.value = voice.id
            val result = synthesisEngine.synthesizeSpeech(
                text = voice.samplePreviewText,
                voice = voice,
                style = VoiceStyle.NATURAL,
                speed = 1.0f,
                onProgress = { _, _, _ -> }
            )

            result.onSuccess { res ->
                audioPlayerManager.loadAudio(res.audioFile.absolutePath, autoPlay = true)
            }
            _previewingVoiceId.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
        synthesisEngine.shutdown()
    }
}
