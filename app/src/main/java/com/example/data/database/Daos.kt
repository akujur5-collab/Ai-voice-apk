package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TtsProjectDao {
    @Query("SELECT * FROM tts_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<TtsProjectEntity>>

    @Query("SELECT * FROM tts_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): TtsProjectEntity?

    @Query("SELECT * FROM tts_projects WHERE title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchProjects(query: String): Flow<List<TtsProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: TtsProjectEntity): Long

    @Query("DELETE FROM tts_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM tts_projects")
    suspend fun clearAllProjects()
}

@Dao
interface FavoriteVoiceDao {
    @Query("SELECT * FROM favorite_voices ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteVoiceEntity>>

    @Query("SELECT voiceId FROM favorite_voices")
    fun getFavoriteVoiceIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteVoiceEntity)

    @Query("DELETE FROM favorite_voices WHERE voiceId = :voiceId")
    suspend fun removeFavorite(voiceId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_voices WHERE voiceId = :voiceId)")
    suspend fun isFavorite(voiceId: String): Boolean
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET hasCompletedOnboarding = 1 WHERE id = 1")
    suspend fun markOnboardingComplete()
}
