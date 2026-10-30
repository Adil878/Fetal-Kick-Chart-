package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ActiveSessionScreen
import com.example.ui.screens.CalendarHistoryScreen
import com.example.ui.screens.ExportReportScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrendsScreen

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TRACKER("Tracker", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    TRENDS("Trends", Icons.Filled.ShowChart, Icons.Outlined.ShowChart),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    EXPORT("Export", Icons.Filled.PictureAsPdf, Icons.Outlined.PictureAsPdf),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    activity: FragmentActivity,
    authViewModel: AuthViewModel,
    sessionViewModel: SessionViewModel,
    historyViewModel: HistoryViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val enteredPin by authViewModel.enteredPin.collectAsStateWithLifecycle()
    val pinErrorMessage by authViewModel.pinErrorMessage.collectAsStateWithLifecycle()

    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    val allSessions by historyViewModel.allSessions.collectAsStateWithLifecycle()
    val selectedDateMillis by historyViewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val selectedSessionEvents by historyViewModel.selectedSessionEvents.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.TRACKER) }

    // Auto-prompt biometric on locked state launch
    LaunchedEffect(authState) {
        if (authState == AuthState.LOCKED && settingsState.isBiometricEnabled) {
            authViewModel.tryBiometricUnlock(activity)
        }
    }

    if (authState != AuthState.UNLOCKED) {
        LockScreen(
            authState = authState,
            enteredPin = enteredPin,
            errorMessage = pinErrorMessage,
            onDigitClick = { authViewModel.onPinDigit(it) },
            onDeleteClick = { authViewModel.onPinDelete() },
            onBiometricClick = { authViewModel.tryBiometricUnlock(activity) },
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentTab) {
                                AppTab.TRACKER -> if (sessionState.isRunning) "Active Session" else "Fetal Kick Tracker"
                                AppTab.TRENDS -> "Activity Trends"
                                AppTab.CALENDAR -> "Movement Calendar"
                                AppTab.EXPORT -> "Doctor Report"
                                AppTab.SETTINGS -> "Settings"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    actions = {
                        if (settingsState.isAppLockEnabled) {
                            IconButton(
                                onClick = { authViewModel.lockApp() },
                                modifier = Modifier.testTag("quick_lock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock App",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = currentTab == tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { targetTab ->
                    when (targetTab) {
                        AppTab.TRACKER -> {
                            ActiveSessionScreen(
                                state = sessionState,
                                onTargetChange = { sessionViewModel.setTargetKicks(it) },
                                onDurationChange = { sessionViewModel.setDurationMinutes(it) },
                                onStartSession = { sessionViewModel.startSession() },
                                onRecordKick = { sessionViewModel.recordKick() },
                                onUndoKick = { sessionViewModel.undoLastKick() },
                                onTogglePause = { sessionViewModel.togglePauseResume() },
                                onRequestFinish = { sessionViewModel.requestManualFinish() },
                                onCancelSession = { sessionViewModel.cancelSession() },
                                onUpdateNotes = { sessionViewModel.updateNotes(it) },
                                onUpdateStrength = { sessionViewModel.updateMovementStrength(it) },
                                onSaveCompleted = {
                                    sessionViewModel.saveCompletedSession {
                                        currentTab = AppTab.CALENDAR
                                    }
                                },
                                onAddWaterGlass = { sessionViewModel.addWaterGlass() },
                                onRemoveWaterGlass = { sessionViewModel.removeWaterGlass() }
                            )
                        }

                        AppTab.TRENDS -> {
                            val trendPoints = remember(allSessions) {
                                historyViewModel.computeTrendPoints(allSessions)
                            }
                            val distribution = remember(allSessions) {
                                historyViewModel.computeTimeOfDayDistribution(allSessions)
                            }
                            TrendsScreen(
                                sessions = allSessions,
                                trendPoints = trendPoints,
                                distribution = distribution
                            )
                        }

                        AppTab.CALENDAR -> {
                            CalendarHistoryScreen(
                                allSessions = allSessions,
                                selectedDateMillis = selectedDateMillis,
                                selectedSessionEvents = selectedSessionEvents,
                                onSelectDate = { historyViewModel.selectDate(it) },
                                onLoadSessionEvents = { historyViewModel.loadEventsForSession(it) },
                                onDeleteSession = { historyViewModel.deleteSession(it) }
                            )
                        }

                        AppTab.EXPORT -> {
                            ExportReportScreen(
                                sessions = allSessions
                            )
                        }

                        AppTab.SETTINGS -> {
                            SettingsScreen(
                                state = settingsState,
                                onDarkModeToggle = { settingsViewModel.setDarkMode(it) },
                                onSystemThemeToggle = { settingsViewModel.setFollowSystemTheme(it) },
                                onDailyReminderToggle = { settingsViewModel.setDailyReminder(it) },
                                onReminderTimeChange = { h, m -> settingsViewModel.setReminderTime(h, m) },
                                onBiometricToggle = { settingsViewModel.setBiometricEnabled(it) },
                                onAppLockToggle = { settingsViewModel.setAppLockEnabled(it) },
                                onChangePin = { authViewModel.initiatePinReset() },
                                onDefaultTargetChange = { settingsViewModel.setDefaultTargetKicks(it) },
                                onDefaultDurationChange = { settingsViewModel.setDefaultDurationMinutes(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
