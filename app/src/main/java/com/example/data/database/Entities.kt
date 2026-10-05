package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tts_projects")
data class TtsProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val text: String,
    val languageCode: String,
    val languageName: String,
    val voiceId: String,
    val voiceName: String,
    val style: String,
    val speed: Float,
    val durationMs: Long,
    val audioFilePath: String,
    val fileSizeBytes: Long,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_voices")
data class FavoriteVoiceEntity(
    @PrimaryKey
    val voiceId: String,
    val languageCode: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val defaultLanguageCode: String = "hi-IN",
    val defaultVoiceId: String = "hi_in_kore",
    val defaultStyle: String = "NATURAL",
    val defaultSpeed: Float = 1.0f,
    val outputFormat: String = "MP3", // "MP3" or "WAV"
    val autoSaveHistory: Boolean = true,
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val hasCompletedOnboarding: Boolean = false,
    val customBackendUrl: String = "",
    val customApiKey: String = ""
)
