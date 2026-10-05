package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioPlayerCard
import com.example.ui.components.LanguageVoiceSelector
import com.example.ui.components.OnboardingDialog
import com.example.ui.components.SampleScriptsDialog
import com.example.ui.components.ShortsModeSelector
import com.example.ui.components.TextToolsBottomSheet
import com.example.ui.components.VoiceStyleAndSpeedSelector
import com.example.ui.components.YouTubeScriptCard
import com.example.ui.viewmodel.GenerationState
import com.example.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showSampleDialog by remember { mutableStateOf(false) }
    var showTextToolsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showOnboarding by remember { mutableStateOf(!uiState.userSettings.hasCompletedOnboarding) }

    LaunchedEffect(uiState.userSettings.hasCompletedOnboarding) {
        if (!uiState.userSettings.hasCompletedOnboarding) {
            showOnboarding = true
        }
    }

    LaunchedEffect(uiState.downloadNotificationMessage) {
        uiState.downloadNotificationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearNotificationMessage()
        }
    }

    LaunchedEffect(uiState.generationState) {
        if (uiState.generationState is GenerationState.Error) {
            val err = (uiState.generationState as GenerationState.Error).message
            snackbarHostState.showSnackbar(err)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VoiceCraft AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "PRO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "AI Text to Speech Studio",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("nav_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Generation History"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("nav_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Script Mode Switcher Tabs
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = if (uiState.isYouTubeMode) 1 else 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mode_tabs"),
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = !uiState.isYouTubeMode,
                        onClick = { viewModel.onYouTubeModeToggled(false) },
                        text = { Text("Standard Script", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tab_standard_mode")
                    )
                    Tab(
                        selected = uiState.isYouTubeMode,
                        onClick = { viewModel.onYouTubeModeToggled(true) },
                        text = { Text("YouTube Script", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.testTag("tab_youtube_mode")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isYouTubeMode) {
                YouTubeScriptCard(
                    hook = uiState.youtubeHook,
                    main = uiState.youtubeMain,
                    cta = uiState.youtubeCta,
                    onPartsChanged = { h, m, c -> viewModel.onYouTubePartsChanged(h, m, c) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Main Text Editor
            Text(
                text = if (uiState.isYouTubeMode) "Compiled Spoken Script" else "Text to Speak",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = uiState.scriptText,
                onValueChange = { viewModel.onScriptTextChanged(it) },
                placeholder = {
                    Text("Type or paste your text here (Hindi, English, Unicode & emojis supported)...")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("script_text_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Counters and Editor Quick Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Characters: ${uiState.characterCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Words: ${uiState.wordCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { viewModel.pasteFromClipboard() },
                        modifier = Modifier.testTag("paste_button")
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste", fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = { viewModel.clearText() },
                        modifier = Modifier.testTag("clear_button")
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = { showSampleDialog = true },
                        modifier = Modifier.testTag("sample_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sample", fontSize = 12.sp)
                    }

                    IconButton(
                        onClick = { showTextToolsSheet = true },
                        modifier = Modifier.testTag("text_tools_button")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Text Tools")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shorts Presets
            ShortsModeSelector(
                selectedPreset = uiState.selectedShortsPreset,
                currentWordCount = uiState.wordCount,
                onPresetSelected = { viewModel.onShortsPresetSelected(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Language & Voice Selector
            LanguageVoiceSelector(
                selectedLanguage = uiState.selectedLanguage,
                selectedVoice = uiState.selectedVoice,
                isFavoriteVoice = uiState.isFavoriteVoice,
                isPreviewLoading = uiState.isPreviewLoading,
                previewingVoiceId = uiState.previewingVoiceId,
                isPlayingPreview = playerState.isPlaying && uiState.previewingVoiceId != null,
                onLanguageSelected = { viewModel.onLanguageSelected(it) },
                onVoiceSelected = { viewModel.onVoiceSelected(it) },
                onToggleFavorite = { viewModel.toggleFavoriteCurrentVoice() },
                onPreviewVoice = { viewModel.previewVoice(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Style and Speed & Output Format
            VoiceStyleAndSpeedSelector(
                selectedStyle = uiState.selectedStyle,
                speed = uiState.speed,
                selectedFormat = uiState.outputFormat,
                onStyleSelected = { viewModel.onStyleSelected(it) },
                onSpeedChanged = { viewModel.onSpeedChanged(it) },
                onFormatSelected = { viewModel.onOutputFormatSelected(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // GENERATE SPEECH Main Button
            val isGenerating = uiState.generationState is GenerationState.Generating ||
                    uiState.generationState is GenerationState.Validating ||
                    uiState.generationState is GenerationState.Processing

            Button(
                onClick = { viewModel.generateSpeech() },
                enabled = !isGenerating && uiState.scriptText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("generate_speech_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    val statusLabel = when (val s = uiState.generationState) {
                        is GenerationState.Validating -> "Validating script..."
                        is GenerationState.Generating -> s.progressText
                        is GenerationState.Processing -> "Finalizing studio audio..."
                        else -> "Generating..."
                    }
                    Text(
                        text = statusLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isYouTubeMode) "GENERATE YOUTUBE VOICE" else "GENERATE SPEECH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Audio Player Card when generation succeeded
            AnimatedVisibility(
                visible = uiState.generationState is GenerationState.Success,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val successState = uiState.generationState as? GenerationState.Success
                if (successState != null) {
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))
                        AudioPlayerCard(
                            playerState = playerState,
                            title = successState.project.title,
                            voiceName = successState.project.voiceName,
                            styleName = successState.project.style,
                            engineUsed = successState.engineUsed,
                            onTogglePlayPause = { viewModel.audioPlayerManager.togglePlayPause() },
                            onStop = { viewModel.audioPlayerManager.stop() },
                            onSeekTo = { viewModel.audioPlayerManager.seekTo(it) },
                            onSeekRelative = { viewModel.audioPlayerManager.seekRelative(it) },
                            onSpeedChange = { viewModel.audioPlayerManager.setPlaybackSpeed(it) },
                            onVolumeChange = { viewModel.audioPlayerManager.setVolume(it) },
                            onDownload = { viewModel.downloadCurrentAudio() },
                            onShare = {
                                val intent = viewModel.createShareIntent()
                                if (intent != null) {
                                    context.startActivity(android.content.Intent.createChooser(intent, "Share VoiceCraft Audio"))
                                } else {
                                    Toast.makeText(context, "Audio file not available to share", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Sample Scripts Dialog
    if (showSampleDialog) {
        SampleScriptsDialog(
            onDismiss = { showSampleDialog = false },
            onSelectSample = { sample ->
                viewModel.applySampleScript(sample)
            }
        )
    }

    // Text Tools Bottom Sheet
    if (showTextToolsSheet) {
        TextToolsBottomSheet(
            sheetState = sheetState,
            characterCount = uiState.characterCount,
            wordCount = uiState.wordCount,
            onCleanText = { viewModel.cleanText() },
            onRemoveExtraSpaces = { viewModel.removeExtraSpaces() },
            onRemoveEmptyLines = { viewModel.removeEmptyLines() },
            onFormatParagraphs = { viewModel.formatParagraphs() },
            onCopyText = { viewModel.copyToClipboard() },
            onPasteText = { viewModel.pasteFromClipboard() },
            onClearText = { viewModel.clearText() },
            onDismiss = { showTextToolsSheet = false }
        )
    }

    // First Launch Onboarding Dialog
    if (showOnboarding) {
        OnboardingDialog(
            onDismiss = {
                showOnboarding = false
                scope.launch {
                    viewModel.repository.markOnboardingComplete()
                }
            }
        )
    }
}
