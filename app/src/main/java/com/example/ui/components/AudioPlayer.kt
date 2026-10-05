package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerManager
import com.example.audio.PlayerState
import java.util.Locale

/**
 * Reusable AudioPlayer component in Compose featuring:
 * - Play / Pause / Stop controls
 * - Seek bar with smooth scrubbing
 * - Precise duration and current time tracking
 * - Equalizer visual indicator
 * - Support for standalone or integrated usage
 */
@Composable
fun AudioPlayer(
    playerState: PlayerState,
    title: String? = null,
    subtitle: String? = null,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onSeekRelative: ((Long) -> Unit)? = null,
    onVolumeChange: ((Float) -> Unit)? = null,
    showControlsExtra: Boolean = true
) {
    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPositionMs by remember { mutableFloatStateOf(0f) }

    val currentDisplayMs = if (isUserScrubbing) {
        scrubPositionMs.toLong()
    } else {
        playerState.currentPositionMs
    }

    val totalDurationMs = playerState.durationMs.coerceAtLeast(1L)
    val sliderProgress = (currentDisplayMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reusable_audio_player"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Title and Equalizer Animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title ?: "Generated Audio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Equalizer wave indicator
                LiveEqualizerWave(isPlaying = playerState.isPlaying)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seek Bar Slider
            Slider(
                value = sliderProgress,
                onValueChange = { progress ->
                    isUserScrubbing = true
                    scrubPositionMs = progress * totalDurationMs
                },
                onValueChangeFinished = {
                    onSeekTo(scrubPositionMs.toLong())
                    isUserScrubbing = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_player_seek_bar"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            // Duration Tracking (Current Position & Total Duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatPlayerDuration(currentDisplayMs),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("audio_player_current_time")
                )
                Text(
                    text = formatPlayerDuration(playerState.durationMs),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("audio_player_total_duration")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transport Controls: [Rewind -5s] [Stop] [Play/Pause] [Forward +5s]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Rewind 5s (optional)
                if (onSeekRelative != null) {
                    IconButton(
                        onClick = { onSeekRelative(-5000L) },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("audio_player_rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 5 seconds",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Stop Button: pauses and resets audio position to 0
                FilledTonalIconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("audio_player_stop_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Play / Pause Main Button
                FilledIconButton(
                    onClick = {
                        if (playerState.isPlaying) {
                            onPause()
                        } else {
                            onPlay()
                        }
                    },
                    modifier = Modifier
                        .size(60.dp)
                        .testTag("audio_player_play_pause_button"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Forward 5s (optional)
                if (onSeekRelative != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { onSeekRelative(5000L) },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("audio_player_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 5 seconds",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Optional Volume Slider
            if (showControlsExtra && onVolumeChange != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = playerState.volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("audio_player_volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(playerState.volume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Convenience overload that connects directly with [AudioPlayerManager].
 */
@Composable
fun AudioPlayer(
    audioPlayerManager: AudioPlayerManager,
    playerState: PlayerState,
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    showControlsExtra: Boolean = true
) {
    AudioPlayer(
        playerState = playerState,
        title = title,
        subtitle = subtitle,
        onPlay = { audioPlayerManager.play() },
        onPause = { audioPlayerManager.pause() },
        onStop = { audioPlayerManager.stop() },
        onSeekTo = { audioPlayerManager.seekTo(it) },
        onSeekRelative = { audioPlayerManager.seekRelative(it) },
        onVolumeChange = { audioPlayerManager.setVolume(it) },
        showControlsExtra = showControlsExtra,
        modifier = modifier
    )
}

/**
 * Animated audio equalizer bars indicating live playback state.
 */
@Composable
fun LiveEqualizerWave(isPlaying: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            val barHeights = listOf(14.dp, 20.dp, 10.dp, 18.dp)
            barHeights.forEachIndexed { index, defaultHeight ->
                val anim = remember { Animatable(8f) }
                LaunchedEffect(isPlaying) {
                    if (isPlaying) {
                        anim.animateTo(
                            targetValue = 20f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 350 + (index * 70), easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                    } else {
                        anim.snapTo(8f)
                    }
                }
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(if (isPlaying) anim.value.dp else defaultHeight)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

/**
 * Formats milliseconds into mm:ss format.
 */
fun formatPlayerDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
