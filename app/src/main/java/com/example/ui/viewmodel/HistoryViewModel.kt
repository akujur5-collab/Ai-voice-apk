package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayerManager
import com.example.audio.AudioStorageManager
import com.example.audio.PlayerState
import com.example.data.database.AppDatabase
import com.example.data.database.TtsProjectEntity
import com.example.data.repository.VoiceCraftRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    val repository = VoiceCraftRepository(database)

    val audioPlayerManager = AudioPlayerManager(context)
    val playerState: StateFlow<PlayerState> = audioPlayerManager.playerState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _playingProjectId = MutableStateFlow<Long?>(null)
    val playingProjectId: StateFlow<Long?> = _playingProjectId.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val projects: StateFlow<List<TtsProjectEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allProjects
            } else {
                repository.searchProjects(query)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun playProject(project: TtsProjectEntity) {
        if (!File(project.audioFilePath).exists()) {
            _snackMessage.value = "Audio file is no longer available on device."
            return
        }
        _playingProjectId.value = project.id
        audioPlayerManager.loadAudio(project.audioFilePath, autoPlay = true)
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            if (_playingProjectId.value == id) {
                audioPlayerManager.release()
                _playingProjectId.value = null
            }
            repository.deleteProject(id)
            _snackMessage.value = "Project removed from history."
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            audioPlayerManager.release()
            _playingProjectId.value = null
            repository.clearAllProjects()
            _snackMessage.value = "History cleared."
        }
    }

    fun downloadProject(project: TtsProjectEntity): Result<Uri> {
        val file = File(project.audioFilePath)
        val result = AudioStorageManager.exportToPublicStorage(context, file, project.title)
        result.onSuccess {
            _snackMessage.value = "Saved audio to Music/VoiceCraft on device!"
        }.onFailure {
            _snackMessage.value = "Export failed: ${it.message}"
        }
        return result
    }

    fun createShareIntent(project: TtsProjectEntity): Intent? {
        val file = File(project.audioFilePath)
        return if (file.exists()) {
            AudioStorageManager.createShareIntent(context, file, project.title)
        } else {
            _snackMessage.value = "Audio file is no longer available."
            null
        }
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
    }
}
