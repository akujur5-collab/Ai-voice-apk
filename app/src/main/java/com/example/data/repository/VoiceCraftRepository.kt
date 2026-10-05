package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.FavoriteVoiceEntity
import com.example.data.database.TtsProjectEntity
import com.example.data.database.UserSettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VoiceCraftRepository(private val database: AppDatabase) {

    private val projectDao = database.ttsProjectDao()
    private val favoriteVoiceDao = database.favoriteVoiceDao()
    private val settingsDao = database.userSettingsDao()

    val allProjects: Flow<List<TtsProjectEntity>> = projectDao.getAllProjects()

    val favoriteVoiceIds: Flow<List<String>> = favoriteVoiceDao.getFavoriteVoiceIds()

    val userSettingsFlow: Flow<UserSettingsEntity?> = settingsDao.getSettingsFlow()

    fun searchProjects(query: String): Flow<List<TtsProjectEntity>> {
        return projectDao.searchProjects(query)
    }

    suspend fun getProjectById(id: Long): TtsProjectEntity? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)
    }

    suspend fun saveProject(project: TtsProjectEntity): Long = withContext(Dispatchers.IO) {
        projectDao.insertProject(project)
    }

    suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(id)
    }

    suspend fun clearAllProjects() = withContext(Dispatchers.IO) {
        projectDao.clearAllProjects()
    }

    suspend fun toggleFavoriteVoice(voiceId: String, languageCode: String): Boolean = withContext(Dispatchers.IO) {
        val isFav = favoriteVoiceDao.isFavorite(voiceId)
        if (isFav) {
            favoriteVoiceDao.removeFavorite(voiceId)
            false
        } else {
            favoriteVoiceDao.addFavorite(FavoriteVoiceEntity(voiceId = voiceId, languageCode = languageCode))
            true
        }
    }

    suspend fun isVoiceFavorite(voiceId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteVoiceDao.isFavorite(voiceId)
    }

    suspend fun getSettings(): UserSettingsEntity = withContext(Dispatchers.IO) {
        settingsDao.getSettings() ?: UserSettingsEntity().also {
            settingsDao.saveSettings(it)
        }
    }

    suspend fun saveSettings(settings: UserSettingsEntity) = withContext(Dispatchers.IO) {
        settingsDao.saveSettings(settings)
    }

    suspend fun markOnboardingComplete() = withContext(Dispatchers.IO) {
        val current = getSettings()
        settingsDao.saveSettings(current.copy(hasCompletedOnboarding = true))
    }
}
