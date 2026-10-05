package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShortsPreset

@Composable
fun ShortsModeSelector(
    selectedPreset: ShortsPreset?,
    currentWordCount: Int,
    onPresetSelected: (ShortsPreset?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 6.dp))
                    Text(
                        text = "Shorts & Reels Duration Target",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Estimate",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Target word limits for short-form pacing. Actual duration depends on language and voice rate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShortsPreset.values().forEach { preset ->
                    val isSelected = selectedPreset == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPresetSelected(preset) },
                        label = {
                            Text(
                                text = "${preset.label}\n~${preset.approxWords}w",
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shorts_preset_${preset.seconds}")
                    )
                }
            }

            if (selectedPreset != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val target = selectedPreset.approxWords
                val diff = currentWordCount - target
                val statusText = when {
                    diff in -10..10 -> "Perfect pacing length! (~${selectedPreset.seconds}s)"
                    diff < -10 -> "Can add ~${-diff} more words to reach ${selectedPreset.seconds}s target."
                    else -> "Script is ~${diff} words longer than recommended ${selectedPreset.seconds}s limit."
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (diff in -10..10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun YouTubeScriptCard(
    hook: String,
    main: String,
    cta: String,
    onPartsChanged: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.padding(start = 6.dp))
                Text(
                    text = "YouTube Creator Script Builder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Structure your video with a compelling Hook, Main narrative, and Call to Action.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Hook Field
            OutlinedTextField(
                value = hook,
                onValueChange = { onPartsChanged(it, main, cta) },
                label = { Text("1. Hook (First 5 seconds)") },
                placeholder = { Text("e.g. क्या आप जानते हैं? / Did you know?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("youtube_hook_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Field
            OutlinedTextField(
                value = main,
                onValueChange = { onPartsChanged(hook, it, cta) },
                label = { Text("2. Main Content & Story") },
                placeholder = { Text("e.g. आज हम एक बहुत ही दिलचस्प बात जानेंगे...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("youtube_main_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(10.dp))

            // CTA Field
            OutlinedTextField(
                value = cta,
                onValueChange = { onPartsChanged(hook, main, it) },
                label = { Text("3. Call to Action (CTA)") },
                placeholder = { Text("e.g. ऐसी ही जानकारी के लिए चैनल को subscribe करें।") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("youtube_cta_input"),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )
        }
    }
}
