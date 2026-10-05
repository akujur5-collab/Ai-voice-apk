package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.UserSettingsEntity
import com.example.data.repository.VoiceCraftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    val repository = VoiceCraftRepository(database)

    private val _settings = MutableStateFlow(UserSettingsEntity())
    val settings: StateFlow<UserSettingsEntity> = _settings.asStateFlow()

    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    init {
        viewModelScope.launch {
            _settings.value = repository.getSettings()
        }
    }

    fun updateTheme(themeMode: String) {
        val updated = _settings.value.copy(themeMode = themeMode)
        save(updated)
    }

    fun updateAutoSaveHistory(enabled: Boolean) {
        val updated = _settings.value.copy(autoSaveHistory = enabled)
        save(updated)
    }

    fun updateDefaultLanguage(langCode: String) {
        val updated = _settings.value.copy(defaultLanguageCode = langCode)
        save(updated)
    }

    fun updateDefaultVoice(voiceId: String) {
        val updated = _settings.value.copy(defaultVoiceId = voiceId)
        save(updated)
    }

    fun updateDefaultStyle(style: String) {
        val updated = _settings.value.copy(defaultStyle = style)
        save(updated)
    }

    fun updateDefaultSpeed(speed: Float) {
        val updated = _settings.value.copy(defaultSpeed = speed)
        save(updated)
    }

    fun updateOutputFormat(format: String) {
        val normalized = if (format.equals("WAV", ignoreCase = true)) "WAV" else "MP3"
        val updated = _settings.value.copy(outputFormat = normalized)
        save(updated)
        _snackMessage.value = "Default output format set to $normalized"
    }

    fun updateBackendConfig(url: String, apiKey: String) {
        val updated = _settings.value.copy(
            customBackendUrl = url.trim(),
            customApiKey = apiKey.trim()
        )
        save(updated)
        _snackMessage.value = "API & Backend settings updated."
    }

    fun clearAppData() {
        viewModelScope.launch {
            repository.clearAllProjects()
            val resetSettings = UserSettingsEntity(id = 1)
            repository.saveSettings(resetSettings)
            _settings.value = resetSettings
            _snackMessage.value = "App data and history cleared."
        }
    }

    private fun save(newSettings: UserSettingsEntity) {
        _settings.value = newSettings
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }
}
