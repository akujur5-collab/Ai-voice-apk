package com.example.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.TtsProjectEntity
import com.example.data.model.Voice
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoicesScreen
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.VoicesViewModel
import androidx.activity.compose.BackHandler

enum class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Studio", Icons.Filled.Mic, Icons.Outlined.Mic),
    VOICES("voices", "Voices", Icons.Filled.RecordVoiceOver, Icons.Outlined.RecordVoiceOver),
    HISTORY("history", "History", Icons.Filled.History, Icons.Outlined.History),
    SETTINGS("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainAppNavigation(
    homeViewModel: HomeViewModel = viewModel(),
    voicesViewModel: VoicesViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }

    // If on a secondary screen, handle Back button to return to Home screen
    if (currentScreen != Screen.HOME) {
        BackHandler {
            currentScreen = Screen.HOME
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                Screen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToHistory = { currentScreen = Screen.HISTORY },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.VOICES -> {
                VoicesScreen(
                    viewModel = voicesViewModel,
                    onVoiceSelected = { voice: Voice ->
                        homeViewModel.onVoiceSelected(voice)
                        currentScreen = Screen.HOME
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.HISTORY -> {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onOpenProjectInEditor = { project: TtsProjectEntity ->
                        homeViewModel.loadProjectIntoEditor(project)
                        currentScreen = Screen.HOME
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
