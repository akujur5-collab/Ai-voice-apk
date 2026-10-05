package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val isPrepared: Boolean = false,
    val currentFilePath: String? = null,
    val errorMessage: String? = null
)

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    fun loadAudio(filePath: String, autoPlay: Boolean = false) {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) {
            _playerState.value = _playerState.value.copy(
                errorMessage = "Audio file is no longer available.",
                isPrepared = false
            )
            return
        }

        try {
            releasePlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                setOnPreparedListener { mp ->
                    val duration = mp.duration.toLong().coerceAtLeast(0L)
                    _playerState.value = _playerState.value.copy(
                        isPrepared = true,
                        durationMs = duration,
                        currentPositionMs = 0L,
                        currentFilePath = filePath,
                        errorMessage = null
                    )
                    applyPlaybackSpeed(_playerState.value.playbackSpeed)
                    if (autoPlay) {
                        play()
                    }
                }
                setOnCompletionListener {
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        currentPositionMs = _playerState.value.durationMs
                    )
                    stopProgressTracking()
                }
                setOnErrorListener { _, what, extra ->
                    _playerState.value = _playerState.value.copy(
                        isPlaying = false,
                        errorMessage = "Playback error ($what, $extra)"
                    )
                    stopProgressTracking()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(
                errorMessage = "Failed to load audio: ${e.message}"
            )
        }
    }

    fun play() {
        val player = mediaPlayer ?: return
        if (!_playerState.value.isPrepared) return

        try {
            if (_playerState.value.currentPositionMs >= _playerState.value.durationMs && _playerState.value.durationMs > 0) {
                player.seekTo(0)
                _playerState.value = _playerState.value.copy(currentPositionMs = 0L)
            }
            player.start()
            _playerState.value = _playerState.value.copy(isPlaying = true)
            startProgressTracking()
        } catch (e: Exception) {
            _playerState.value = _playerState.value.copy(errorMessage = e.message)
        }
    }

    fun pause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _playerState.value = _playerState.value.copy(isPlaying = false)
            stopProgressTracking()
        }
    }

    fun togglePlayPause() {
        if (_playerState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun stop() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        }
        player.seekTo(0)
        _playerState.value = _playerState.value.copy(
            isPlaying = false,
            currentPositionMs = 0L
        )
        stopProgressTracking()
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        if (!_playerState.value.isPrepared) return
        val clamped = positionMs.coerceIn(0L, _playerState.value.durationMs)
        player.seekTo(clamped.toInt())
        _playerState.value = _playerState.value.copy(currentPositionMs = clamped)
    }

    fun seekRelative(deltaMs: Long) {
        val current = _playerState.value.currentPositionMs
        seekTo(current + deltaMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playerState.value = _playerState.value.copy(playbackSpeed = speed)
        applyPlaybackSpeed(speed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        val player = mediaPlayer ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && _playerState.value.isPrepared) {
            try {
                val params = player.playbackParams ?: PlaybackParams()
                params.speed = speed
                player.playbackParams = params
            } catch (e: Exception) {
                // Ignore if unsupported
            }
        }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _playerState.value = _playerState.value.copy(volume = clamped)
        mediaPlayer?.setVolume(clamped, clamped)
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playerState.value = _playerState.value.copy(
                            currentPositionMs = player.currentPosition.toLong(),
                            durationMs = player.duration.toLong().coerceAtLeast(0L)
                        )
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracking()
        releasePlayer()
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        } finally {
            mediaPlayer = null
        }
    }
}
