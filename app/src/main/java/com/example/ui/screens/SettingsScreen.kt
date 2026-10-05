package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VoiceCatalog
import com.example.data.model.VoiceStyle
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val snackMessage by viewModel.snackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showBackendConfigDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
            // Theme Mode Section
            SectionHeader(title = "Appearance & Theme")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("App Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark").forEach { (mode, label) ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.updateTheme(mode) },
                                label = { Text(label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_${mode.lowercase()}_chip")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Studio Defaults Section
            SectionHeader(title = "Studio Defaults")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Auto-save switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Save to History", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Automatically store generated audio and scripts for offline access.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.autoSaveHistory,
                            onCheckedChange = { viewModel.updateAutoSaveHistory(it) },
                            modifier = Modifier.testTag("autosave_history_switch")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Default Language
                    SettingRowItem(
                        icon = Icons.Default.Language,
                        title = "Default Language",
                        subtitle = VoiceCatalog.getLanguageByCode(settings.defaultLanguageCode).name,
                        onClick = {
                            // Cycle through top languages or Hindi / English
                            val next = if (settings.defaultLanguageCode == "hi-IN") "en-US" else "hi-IN"
                            viewModel.updateDefaultLanguage(next)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Default Voice Style
                    SettingRowItem(
                        icon = Icons.Default.Tune,
                        title = "Default Voice Style",
                        subtitle = settings.defaultStyle,
                        onClick = {
                            val styles = VoiceStyle.values()
                            val curIdx = styles.indexOfFirst { it.name == settings.defaultStyle }
                            val nextStyle = styles[(curIdx + 1) % styles.size].name
                            viewModel.updateDefaultStyle(nextStyle)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Default Output Audio Format (MP3 vs WAV)
                    SettingRowItem(
                        icon = Icons.Default.Audiotrack,
                        title = "Default Output Format",
                        subtitle = "${settings.outputFormat} • ${if (settings.outputFormat == "MP3") "Compressed (Smaller file size, universal)" else "Studio Master WAV (Lossless 24kHz PCM)"}",
                        onClick = {
                            val next = if (settings.outputFormat == "MP3") "WAV" else "MP3"
                            viewModel.updateOutputFormat(next)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // API & Backend Integration
            SectionHeader(title = "AI Cloud & Backend Engine")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRowItem(
                        icon = Icons.Default.Cloud,
                        title = "Backend Service & API Key",
                        subtitle = if (settings.customBackendUrl.isNotBlank())
                            "Custom: ${settings.customBackendUrl.take(28)}..."
                        else "Built-in Google Gemini AI & Device HD Synthesis",
                        onClick = { showBackendConfigDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingRowItem(
                        icon = Icons.Default.Folder,
                        title = "Audio Storage Location",
                        subtitle = "Android Music/VoiceCraft & App Cache (Zero broad storage permissions needed)",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Privacy & Data
            SectionHeader(title = "Privacy & Data")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingRowItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy & Transparency",
                        subtitle = "How your script text and generated audio are handled",
                        onClick = { showPrivacyDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingRowItem(
                        icon = Icons.Default.Info,
                        title = "About VoiceCraft AI",
                        subtitle = "Version ${com.example.BuildConfig.VERSION_NAME} • Professional AI Text-to-Speech Studio",
                        onClick = { showAboutDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    SettingRowItem(
                        icon = Icons.Default.Download,
                        title = "Download Latest APK ↗",
                        subtitle = "Check for updates and download official releases on GitHub",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/akujur5-collab/Text-to-voice-.-Apk/releases/latest")
                            )
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Clear Data
                    SettingRowItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Reset App Data & Cache",
                        subtitle = "Deletes all locally saved history, projects and cached files",
                        isDestructive = true,
                        onClick = { showClearDataConfirm = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Backend Config Dialog
    if (showBackendConfigDialog) {
        var tempUrl by remember { mutableStateOf(settings.customBackendUrl) }
        var tempApiKey by remember { mutableStateOf(settings.customApiKey) }

        AlertDialog(
            onDismissRequest = { showBackendConfigDialog = false },
            title = { Text("AI TTS API & Backend Settings") },
            text = {
                Column {
                    Text(
                        text = "VoiceCraft AI connects to Google's official Gemini audio generation model (gemini-2.5-flash-preview-tts) or your custom proxy server.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("Proxy Backend URL (Optional)") },
                        placeholder = { Text("https://your-server.run.app/api/tts") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        label = { Text("Custom Gemini API Key (Optional)") },
                        placeholder = { Text("Leave blank to use pre-configured key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateBackendConfig(tempUrl, tempApiKey)
                        showBackendConfigDialog = false
                    }
                ) {
                    Text("Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackendConfigDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy & Data Security") },
            text = {
                Column {
                    Text(
                        text = "• AI Speech Generation: Your script text is securely transmitted to Google's AI services (Gemini TTS API) solely to synthesize requested audio.\n\n" +
                                "• Local Storage: All generated audio files and project history are saved locally on your device in your app storage or Music folder.\n\n" +
                                "• Zero Unnecessary Permissions: VoiceCraft AI does not request contacts, camera, or microphone permissions.\n\n" +
                                "• Full Ownership: You own 100% of your generated audio and scripts for YouTube, social media, and commercial use.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About VoiceCraft AI") },
            text = {
                Column {
                    Text(
                        text = "VoiceCraft AI is a professional mobile AI Text-to-Speech studio tailored for YouTube creators, Shorts creators, narrators, devotional readers, and educators.\n\n" +
                                "Built with Kotlin, Jetpack Compose, Material 3, and Google Gemini AI audio models with offline fallback synthesis.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/akujur5-collab/Text-to-voice-.-Apk/releases/latest")
                    )
                    context.startActivity(intent)
                }) {
                    Text("Releases ↗")
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Reset App Data?") },
            text = { Text("This will permanently delete all saved history, favorite voices, and cached audio files from VoiceCraft AI.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAppData()
                        showClearDataConfirm = false
                    }
                ) {
                    Text("Reset All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
